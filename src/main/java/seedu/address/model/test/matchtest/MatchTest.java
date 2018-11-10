package seedu.address.model.test.matchtest;

import static seedu.address.commons.util.AppUtil.checkArgument;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import seedu.address.commons.core.EventsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.events.ui.FlashMatchOutcomeEvent;
import seedu.address.commons.events.ui.ShowTriviaTestResultEvent;
import seedu.address.model.ReadOnlyTriviaBundle;
import seedu.address.model.card.Answer;
import seedu.address.model.card.Card;
import seedu.address.model.card.IndexedAnswer;
import seedu.address.model.card.IndexedQuestion;
import seedu.address.model.card.Question;
import seedu.address.model.test.TestType;
import seedu.address.model.test.TriviaTest;
import seedu.address.model.test.matchtest.exceptions.AnswerNotFoundException;
import seedu.address.model.test.matchtest.exceptions.QuestionNotFoundException;
import seedu.address.model.topic.Topic;
import seedu.address.ui.UiPart;
import seedu.address.ui.test.TriviaTestPage;
import seedu.address.ui.test.TriviaTestResultPage;
import seedu.address.ui.test.matchtest.MatchTestPage;
import seedu.address.ui.test.matchtest.MatchTestResultPage;

/**
 * Represents a trivia test that is started by the user.
 * For a {@code MatchTest} to start, there must be more than 1 cards related to the topic that is specified in the test.
 */
public class MatchTest extends TriviaTest {
    public static final String MESSAGE_MATCH_TEST_CONSTRAINS = "Unable to start MatchTest with %d card"
            + " associated to the topic of %s.\n"
            + "Need more than 1 card to proceed.";

    public final TestType testType = TestType.MATCH_TEST;
    private List<MatchAttempt> attempts;

    private final ObservableList<IndexedQuestion> shuffledQuestions;
    private final ObservableList<IndexedAnswer> shuffledAnswers;

    public MatchTest(Topic topic, ReadOnlyTriviaBundle triviaBundle) {
        super(topic, triviaBundle);

        shuffledQuestions = getQuestions(cards);
        shuffledAnswers = getAnswers(cards);

        attempts = new ArrayList<>();

        checkArgument(isValidMatchTest(), String.format(MESSAGE_MATCH_TEST_CONSTRAINS, cards.size(), topic.topicName));
    }

    /**
     * The logic associated to matching a question and a answer.
     *
     * @param questionIndex The index of the question to match.
     * @param answerIndex The index of the answer to match.
     * @return a boolean which signify whether the match is success or failure.
     */
    public boolean match(Index questionIndex, Index answerIndex) throws QuestionNotFoundException,
            AnswerNotFoundException {
        MatchAttempt attempt = addAttempt(questionIndex, answerIndex);
        postOutcomeOfMatch(attempt);

        if (!attempt.isCorrect()) {
            return false;
        }
        if (isAtLastMatch()) {
            isCompleted = true;
        }

        Timer delayForUi = new Timer();
        delayForUi.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    respondToCorrectAttempt(attempt);
                });
            }
        }, UiPart.FLASH_TIME);
        return true;
    }

    @Override
    public TestType getTestType() {
        return testType;
    }

    @Override
    public List<MatchAttempt> getAttempts() {
        return attempts;
    }

    /**
     * Responds to an correct attempt accordingly.
     */
    public void respondToCorrectAttempt(MatchAttempt attempt) {
        assert attempt.isCorrect();

        if (isAtLastMatch()) {
            stopTest();
            EventsCenter.getInstance().post(new ShowTriviaTestResultEvent(getResultPage()));
        }
        removeCardFromUi(attempt);
    }

    /**
     * Add and return an attempt to the Match Test.
     *
     * @param questionIndex The index of the question to match.
     * @param answerIndex The index of the answer to match.
     * @return the new Matching attempt.
     * @throws IndexOutOfBoundsException when the given index is out of range of the given answers or questions.
     */
    private MatchAttempt addAttempt(Index questionIndex, Index answerIndex) throws QuestionNotFoundException,
            AnswerNotFoundException {

        IndexedQuestion question = getIndexedQuestion(questionIndex);
        IndexedAnswer answer = getIndexedAnswer(answerIndex);
        Card attemptedCard = getAttemptedCard(question);

        MatchAttempt newAttempt = new MatchAttempt(attemptedCard, question, answer);
        attempts.add(newAttempt);
        return newAttempt;
    }

    /**
     * Will return the IndexedQuestion that was attempted. Note that the parameter of {@code questionIndex} can be null.
     * If {@code questionIndex} is null, the first the IndexedQuestion in the list of shuffledQuestions will be
     * returned.
     *
     * Also note that it uses the Id that is associated to IndexedQuestion to identify the attempted IndexedQuestion.
     */
    private IndexedQuestion getIndexedQuestion(Index questionIndex) throws QuestionNotFoundException {
        Optional<Index> optionalQuestionIndex = Optional.ofNullable(questionIndex);

        return optionalQuestionIndex.map(selectedIndex ->
                shuffledQuestions.stream()
                        .filter(q -> q.getId() == selectedIndex.getOneBased())
                        .findFirst()
                        .orElseThrow(QuestionNotFoundException::new)
        ).orElse(shuffledQuestions.stream()
                .findFirst()
                .orElseThrow(QuestionNotFoundException::new));
    }

    /**
     * Will return the IndexedAnswer that was matched.
     *
     * Note that it uses the Id that is associated to IndexedAnswer to identify the selected IndexedAnswer.
     */
    private IndexedAnswer getIndexedAnswer(Index answerIndex) throws AnswerNotFoundException {
        return shuffledAnswers.stream()
                .filter(q -> q.getId() == answerIndex.getOneBased())
                .findFirst()
                .orElseThrow(AnswerNotFoundException::new);
    }

    /**
     * Will return the attempted card which is based on which IndexedQuestion did the user match.
     */
    private Card getAttemptedCard(IndexedQuestion question) {
        return cards.stream()
                .filter(card -> card.getQuestion().equals(question))
                .findFirst()
                .orElseThrow(QuestionNotFoundException::new);
    }

    /**
     * Remove the involved card that is answered correctly from the UI.
     * @param attempt The attempt that was made by the user in the Match Test.
     */
    private void removeCardFromUi(MatchAttempt attempt) {
        assert attempt.isCorrect();

        shuffledQuestions.remove(attempt.getIndexedQuestion());
        shuffledAnswers.remove(attempt.getIndexedAnswer());
    }

    /**
     * Will create an UI event to indicate on the UI on whether the match is successful or not.
     * @param attempt The attempt of that match command.
     */
    private void postOutcomeOfMatch(MatchAttempt attempt) {
        EventsCenter.getInstance().post(new FlashMatchOutcomeEvent(
                shuffledQuestions.indexOf(attempt.getIndexedQuestion()),
                shuffledAnswers.indexOf(attempt.getIndexedAnswer()), attempt.isCorrect()));
    }

    private boolean isValidMatchTest() {
        return cards.size() > 1;
    }

    /**
     * Starts the timer of the Match Test.
     */
    private void startTimer() {
        DecimalFormat timerFormat = new DecimalFormat("#.#");
        timer = new Timeline(new KeyFrame(Duration.seconds(0.1), ev -> {
            duration.setValue(Double.parseDouble(timerFormat.format(duration.getValue() + 0.1)));
        }));
        timer.setCycleCount(Animation.INDEFINITE);
        timer.play();
    }

    public ObservableList<IndexedQuestion> getQuestions() {
        return this.shuffledQuestions;
    }

    /**
     * Retrieve a randomized modifiable observable list of questions to allow changes in the UI.
     * @param cards The cards to retrieve the questions from.
     * @return an observable list of questions
     */
    private ObservableList<IndexedQuestion> getQuestions(List<Card> cards) {
        List<Question> questions = cards.stream()
                .map(Card::getQuestion)
                .collect(Collectors.toList());

        Collections.shuffle(questions);

        List<IndexedQuestion> indexedQuestions = IntStream.range(0, questions.size())
                .mapToObj(index -> new IndexedQuestion(questions.get(index), index + 1))
                .collect(Collectors.toList());

        return FXCollections.observableList(indexedQuestions);
    }

    public ObservableList<IndexedAnswer> getAnswers() {
        return this.shuffledAnswers;
    }

    /**
     * Retrieve a randomized modifiable observable list of answers to allow changes in the UI.
     * @param cards The cards to retrieve the answers from.
     * @return an observable list of answers
     */
    private ObservableList<IndexedAnswer> getAnswers(List<Card> cards) {
        List<Answer> answers = cards.stream()
                .map(Card::getAnswer)
                .collect(Collectors.toList());

        Collections.shuffle(answers);

        List<IndexedAnswer> indexedAnswers = IntStream.range(0, answers.size())
                .mapToObj(index -> new IndexedAnswer(answers.get(index), index + 1))
                .collect(Collectors.toList());

        return FXCollections.observableList(indexedAnswers);
    }

    private boolean isAtLastMatch() {
        return shuffledQuestions.size() == 1 && shuffledAnswers.size() == 1;
    }

    @Override
    public void startTest() {
        startTimer();
    }

    @Override
    public void stopTest() {
        timer.stop();
    }

    @Override
    public Supplier<? extends TriviaTestPage> getTestingPage() { return () -> new MatchTestPage(this); }

    @Override
    public Supplier<? extends TriviaTestResultPage> getResultPage() {
        return () -> new MatchTestResultPage(this);
    }

    @Override
    public boolean equals(Object obj) {
        // short circuit if same object
        if (obj == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(obj instanceof MatchTest)) {
            return false;
        }

        // state check
        MatchTest other = (MatchTest) obj;
        return cards.equals(other.cards) && attempts.equals(other.attempts);
    }
}
