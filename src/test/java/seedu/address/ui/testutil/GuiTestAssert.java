package seedu.address.ui.testutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import guitests.guihandles.CardInfoPanelHandle;
import guitests.guihandles.CardListPanelHandle;
import guitests.guihandles.CardViewHandle;
import guitests.guihandles.PersonCardHandle;
import guitests.guihandles.PersonListPanelHandle;
import guitests.guihandles.ResultDisplayHandle;
import seedu.address.model.card.Card;
import seedu.address.model.person.Person;
import seedu.address.model.test.Attempt;

/**
 * A set of assertion methods useful for writing GUI tests.
 */
public class GuiTestAssert {
    /**
     * Asserts that {@code actualCard} displays the same values as {@code expectedCard}.
     */
    public static void assertCardEquals(PersonCardHandle expectedCard, PersonCardHandle actualCard) {
        assertEquals(expectedCard.getId(), actualCard.getId());
        assertEquals(expectedCard.getAddress(), actualCard.getAddress());
        assertEquals(expectedCard.getEmail(), actualCard.getEmail());
        assertEquals(expectedCard.getName(), actualCard.getName());
        assertEquals(expectedCard.getPhone(), actualCard.getPhone());
        assertEquals(expectedCard.getTags(), actualCard.getTags());
    }

    /**
     * Asserts that {@code actualCard} displays the same values as {@code expectedCard}.
     */
    public static void assertCardEquals(CardViewHandle expectedCard, CardViewHandle actualCard) {
        assertEquals(expectedCard.getId(), actualCard.getId());
        assertEquals(expectedCard.getQuestion(), actualCard.getQuestion());
        assertEquals(expectedCard.getAnswer(), actualCard.getAnswer());
        assertEquals(expectedCard.getTopics(), actualCard.getTopics());
    }

    /**
     * Asserts that {@code actualCard} displays the details of {@code expectedPerson}.
     */
    public static void assertCardDisplaysPerson(Person expectedPerson, PersonCardHandle actualCard) {
        assertEquals(expectedPerson.getName().fullName, actualCard.getName());
        assertEquals(expectedPerson.getPhone().value, actualCard.getPhone());
        assertEquals(expectedPerson.getEmail().value, actualCard.getEmail());
        assertEquals(expectedPerson.getAddress().value, actualCard.getAddress());
        assertEquals(expectedPerson.getTags().stream().map(tag -> tag.topicName).collect(Collectors.toList()),
                actualCard.getTags());
    }

    /**
     * Asserts that the {@code expectedCardInfoPanel} displays the details of {@code expectedCard}
     */
    public static void assertCardInfoPanelDisplaysCard(Card expectedCard, List<Attempt> attemptsOnCard,
                                                       CardInfoPanelHandle actualCardInfoPanel) {

        assertEquals(expectedCard.getQuestion().value, actualCardInfoPanel.getQuestion());
        assertEquals(expectedCard.getAnswer().value, actualCardInfoPanel.getCardInfoAnswer());
        assertEquals(expectedCard.getTopics().stream().map(topic -> topic.topicName).collect(Collectors.toList()),
                actualCardInfoPanel.getCardInfoTopics());
        assertTrue(actualCardInfoPanel.getCardExperienceHandle().equals(attemptsOnCard));

        attemptsOnCard.stream()
                .filter(attempt -> !attempt.isCorrect())
                .max(Comparator.comparing(Attempt::getTimestamp))
                .ifPresentOrElse(attempt ->
                        assertTrue(actualCardInfoPanel.getCardMostRecentMistakeHandle()
                                .equals(expectedCard, attempt)), () ->
                        assertTrue(actualCardInfoPanel.getCardMostRecentMistakeHandle()
                                .equals(expectedCard, null)));
    }

    /**
     * Asserts that {@code actualCard} displays the details of {@code expectedCard}.
     */
    public static void assertCardViewDisplay(Card expectedCard, CardViewHandle actualCard) {
        assertEquals(expectedCard.getQuestion().value, actualCard.getQuestion());
        assertEquals(expectedCard.getAnswer().value, actualCard.getAnswer());
        assertEquals(expectedCard.getTopics().stream().map(topic -> topic.topicName).collect(Collectors.toList()),
                actualCard.getTopics());
    }

    /**
     * Asserts that the list in {@code personListPanelHandle} displays the details of {@code persons} correctly and
     * in the correct order.
     */
    public static void assertListMatching(PersonListPanelHandle personListPanelHandle, Person... persons) {
        for (int i = 0; i < persons.length; i++) {
            personListPanelHandle.navigateToCard(i);
            assertCardDisplaysPerson(persons[i], personListPanelHandle.getPersonCardHandle(i));
        }
    }

    /**
     * Asserts that the list in {@code cardListPanelHandle} displays the details of {@code cards} correctly and
     * in the correct order.
     */
    public static void assertListMatching(CardListPanelHandle cardListPanelHandle, Card... cards) {
        for (int i = 0; i < cards.length; i++) {
            cardListPanelHandle.navigateToCard(i);
            assertCardViewDisplay(cards[i], cardListPanelHandle.getCardViewHandle(i));
        }
    }

    /**
     * Asserts that the list in {@code personListPanelHandle} displays the details of {@code persons} correctly and
     * in the correct order.
     */
    public static void assertListMatching(PersonListPanelHandle personListPanelHandle, List<Person> persons) {
        assertListMatching(personListPanelHandle, persons.toArray(new Person[0]));
    }

    /**
     * Asserts that the list in {@code personListPanelHandle} displays the details of {@code persons} correctly and
     * in the correct order.
     */
    public static void assertListMatching(CardListPanelHandle cardListPanelHandle, List<Card> cards) {
        assertListMatching(cardListPanelHandle, cards.toArray(new Card[0]));
    }

    /**
     * Asserts the size of the list in {@code personListPanelHandle} equals to {@code size}.
     */
    public static void assertListSize(PersonListPanelHandle personListPanelHandle, int size) {
        int numberOfPeople = personListPanelHandle.getListSize();
        assertEquals(size, numberOfPeople);
    }

    /**
     * Asserts the message shown in {@code resultDisplayHandle} equals to {@code expected}.
     */
    public static void assertResultMessage(ResultDisplayHandle resultDisplayHandle, String expected) {
        assertEquals(expected, resultDisplayHandle.getText());
    }
}
