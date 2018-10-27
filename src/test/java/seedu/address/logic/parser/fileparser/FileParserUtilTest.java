package seedu.address.logic.parser.fileparser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import seedu.address.logic.parser.fileparser.exceptions.FileParseException;
import seedu.address.model.card.Answer;
import seedu.address.model.card.Card;
import seedu.address.model.card.Question;
import seedu.address.model.topic.Topic;
import seedu.address.testutil.Assert;

public class FileParserUtilTest {
    private static final String QUESTION_ANSWER_PAIR_SEPARATOR = "\t";

    private static final String WHITESPACE = " \t\r\n";
    private static final String VALID_TOPIC_1 = " t/topic1";
    private static final String VALID_TOPIC_1_WITHOUT_PREFIX = "topic1";
    private static final String VALID_TOPIC_2 = " t/topic2";
    private static final String VALID_TOPIC_2_WITHOUT_PREFIX = "topic2";
    private static final String VALID_QUESTION_ANSWER_PAIR = "question\tanswer";
    private static final String VALID_QUESTION = "question";
    private static final String VALID_ANSWER = "answer";

    private static final String[] VALID_CARD_STRING = {"question", "answer"};

    private static final String INVALID_EMPTY_TOPIC = " ";
    private static final String INVALID_TOPIC = "topic";
    private static final String INVALID_QUESTION_ANSWER_PAIR_WITHOUT_TAB = "question answer";
    private static final String INVALID_QUESTION_ANSWER_PAIR_WITHOUT_ANSWER = "question\t";
    private static final String INVALID_QUESTION_ANSWER_PAIR_WITH_EXTRA_SUFFIX = "question\tanswer\textra";
    private static final String[] INVALID_CARD_STRING = {"question"};

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void isEmpty_null_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class, () -> FileParserUtil.isEmpty((String) null));
    }

    @Test
    public void isEmpty_emptyValue_returnsTrue() {
        assertTrue(FileParserUtil.isEmpty(WHITESPACE));
    }

    @Test
    public void isEmpty_validValueWithWhitespacePreamble_returnsFalse() {
        assertFalse(FileParserUtil.isEmpty(WHITESPACE + VALID_TOPIC_1));
    }

    @Test
    public void isTopic_null_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class, () -> FileParserUtil.isTopic((String) null));
    }

    @Test public void isTopic_inValidValue_throwsFileParseException() {
        Assert.assertThrows(FileParseException.class, () -> FileParserUtil.isTopic((INVALID_EMPTY_TOPIC)));
    }

    @Test public void isTopic_inValidValue_returnsFalse() {
        assertFalse(FileParserUtil.isTopic(INVALID_TOPIC));
    }

    @Test public void isTopic_validValue_returnsTrue() {
        assertTrue(FileParserUtil.isTopic(VALID_TOPIC_1));
    }

    @Test
    public void parseLineToTopicSet_null_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class, () -> FileParserUtil.parseLineToTopicSet((String) null));
    }

    @Test
    public void parseLineToTopicSet_invalidValue_throwsFileParseException() {
        Assert.assertThrows(FileParseException.class, () -> FileParserUtil.parseLineToTopicSet(INVALID_TOPIC));
    }

    @Test
    public void parseLineToTopicSet_invalidValueCollection_throwsFileParseException() {
        Assert.assertThrows(FileParseException.class, () ->
                FileParserUtil.parseLineToTopicSet(VALID_TOPIC_1 + " " + INVALID_TOPIC));
    }

    @Test
    public void parseLineToTopicSet_validValueCollection_returnsTopicSet() {
        Set<Topic> expectedTopicSet = new HashSet<>(Arrays.asList(new Topic(VALID_TOPIC_1_WITHOUT_PREFIX),
                new Topic(VALID_TOPIC_2_WITHOUT_PREFIX)));
        Set<Topic> actualTopicSet = FileParserUtil.parseLineToTopicSet(VALID_TOPIC_1 + VALID_TOPIC_2);

        assertEquals(expectedTopicSet, actualTopicSet);
    }

    @Test
    public void parseLineToQuestionAnswerPair_null_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class, () ->
                FileParserUtil.parseLineToQuestionAnswerPair((String) null));
    }

    @Test
    public void parseLineToQuestionAnswerPair_invalidValueWithoutAnswer_throwsFileParseException() {
        Assert.assertThrows(FileParseException.class, () ->
                FileParserUtil.parseLineToQuestionAnswerPair(INVALID_QUESTION_ANSWER_PAIR_WITHOUT_ANSWER));
    }

    @Test
    public void parseLineToQuestionAnswerPair_invalidValueWithExtraInfo_throwsFileParseException() {
        Assert.assertThrows(FileParseException.class, () ->
                FileParserUtil.parseLineToQuestionAnswerPair(INVALID_QUESTION_ANSWER_PAIR_WITH_EXTRA_SUFFIX));
    }

    @Test
    public void parseLineToQuestionAnswerPair_invalidValueWithoutTab_throwsFileParseException() {
        Assert.assertThrows(FileParseException.class, () ->
                FileParserUtil.parseLineToQuestionAnswerPair(INVALID_QUESTION_ANSWER_PAIR_WITHOUT_TAB));
    }

    @Test
    public void parseLineToTopicSet_validValue_returnsQuestionAnswerPair() {
        String[] expectedCardStringPair = VALID_QUESTION_ANSWER_PAIR.split(QUESTION_ANSWER_PAIR_SEPARATOR);
        String[] actualCardStringPair = FileParserUtil.parseLineToQuestionAnswerPair(VALID_QUESTION_ANSWER_PAIR);

        assertEquals(expectedCardStringPair, actualCardStringPair);
    }

    // topic valid, valid with empty topic
    @Test
    public void stringToCard_nullCardString_throwsNullPointerException() {
        Set<Topic> validTopicSet = new HashSet<>(Arrays.asList(new Topic(VALID_TOPIC_1_WITHOUT_PREFIX),
                new Topic(VALID_TOPIC_2_WITHOUT_PREFIX)));

        Assert.assertThrows(NullPointerException.class, () ->
                FileParserUtil.stringToCard((String[]) null, validTopicSet));
    }

    @Test
    public void stringToCard_nullTopicSet_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class, () ->
                FileParserUtil.stringToCard(VALID_CARD_STRING, (Set<Topic>) null));
    }

    @Test
    public void stringToCard_inValidValue_throwsFileParseException() {
        Set<Topic> emptySet = new HashSet<>();
        Assert.assertThrows(FileParseException.class, () ->
                FileParserUtil.stringToCard(INVALID_CARD_STRING, emptySet));
    }

    @Test
    public void stringToCard_validValueNoTopic_returnsCard() {
        Set<Topic> emptySet = new HashSet<>();
        Card expectedCard = new Card(new Question(VALID_QUESTION), new Answer(VALID_ANSWER), emptySet);
        Card actualCard = FileParserUtil.stringToCard(VALID_CARD_STRING, emptySet);

        assertEquals(expectedCard, actualCard);
    }

    @Test
    public void stringToCard_validValueWithTopic_returnsCard() {
        Set<Topic> validTopicSet = new HashSet<>(Arrays.asList(new Topic(VALID_TOPIC_1_WITHOUT_PREFIX),
                new Topic(VALID_TOPIC_2_WITHOUT_PREFIX)));

        Card expectedCard = new Card(new Question(VALID_QUESTION), new Answer(VALID_ANSWER), validTopicSet);
        Card actualCard = FileParserUtil.stringToCard(VALID_CARD_STRING, validTopicSet);

        assertEquals(expectedCard, actualCard);
    }
}
