package seedu.address.model.test.matchtest;

import static seedu.address.commons.util.AppUtil.checkArgument;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import seedu.address.commons.core.EventsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.events.ui.FlashMatchOutcomeEvent;
import seedu.address.model.ReadOnlyTriviaBundle;
import seedu.address.model.card.Answer;
import seedu.address.model.card.Card;
import seedu.address.model.card.Question;
import seedu.address.model.test.TriviaTest;
import seedu.address.model.topic.Topic;

/**
 * Represents a trivia test that is started by the user.
 * For a {@code MatchTest} to start, there must be more than 1 cards related to the topic that is specified in the test.
 */
public class MatchTest extends TriviaTest {
    public static final String MESSAGE_MATCH_TEST_CONSTRAINS = "Matching test needs more than 1 card with the"
            + " corresponding topic to proceed.";

    private final List<MatchAttempt> attempts;

    private double duration;
    private Timer timer;

    public MatchTest(Topic tag, ReadOnlyTriviaBundle triviaBundle) {
        super(tag, triviaBundle);

        checkArgument(isValidMatchTest(), MESSAGE_MATCH_TEST_CONSTRAINS);
        attempts = new ArrayList<>();
    }

    /**
     *
     * @return a boolean to indicate whether the test has come to an end
     */
    public boolean isEndOfTest() {
        return questions.isEmpty() && answers.isEmpty();
    }

    /**
     * Remove the involved card that is answered correctly from the UI.
     * @param attempt The attempt that was made by the user in the matching test.
     */
    public void removeCardFromUi(MatchAttempt attempt) {
        assert attempt.isCorrect(); // Ensure that attempt is correct before removing.

        questions.remove(attempt.getCardWithQuestion().getQuestion());
        answers.remove(attempt.getCardWithAnswer().getAnswer());
    }

    /**
     * Add an attempt to the matching test.
     *
     * @param questionIndex The index of the question to match.
     * @param answerIndex The index of the answer to match.
     * @return the new Matching attempt.
     * @throws IndexOutOfBoundsException when the given index is out of range of the given answers or questions.
     */
    public MatchAttempt addAttempt(Index questionIndex, Index answerIndex) throws IndexOutOfBoundsException {
        Question question = questions.get(questionIndex.getZeroBased());
        Answer answer = answers.get(answerIndex.getZeroBased());

        Card cardWithQuestion = cards.stream()
                .filter(card -> card.getQuestion().equals(question))
                .findFirst()
                .orElseThrow(IndexOutOfBoundsException::new);
        Card cardWithAnswer = cards.stream()
                .filter(card -> card.getAnswer().equals(answer))
                .findFirst()
                .orElseThrow(IndexOutOfBoundsException::new);

        MatchAttempt newAttempt = new MatchAttempt(cardWithQuestion, cardWithAnswer);
        attempts.add(newAttempt);
        return newAttempt;
    }

    /**
     * Will create an UI event to indicate on the UI on whether the match is successful or not.
     * @param attempt The attempt of that match command.
     */
    public void postOutcomeOfMatch(MatchAttempt attempt) {
        int indexOfQuestion = getQuestions().indexOf(attempt.getCardWithQuestion().getQuestion());
        int indexOfAnswer = getAnswers().indexOf(attempt.getCardWithAnswer().getAnswer());
        EventsCenter.getInstance().post(new FlashMatchOutcomeEvent(indexOfQuestion, indexOfAnswer,
                attempt.isCorrect()));
    }

    private boolean isValidMatchTest() {
        return super.getQuestions().size() > 1;
    }

    @Override
    public void startTest() {
        duration = 0;
        timer = new Timer();
        DecimalFormat timerFormat = new DecimalFormat("#.#");
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                duration = Double.parseDouble(timerFormat.format(duration + 0.1));
                System.out.println("Seconds passed: " + duration);
            }
        };

        timer.scheduleAtFixedRate(task, 0, 100);

    }

    @Override
    public void stopTest() {
        // TODO: Show the result screen.
        // TODO: Record results if test did not end prematurely.
        timer.cancel();
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