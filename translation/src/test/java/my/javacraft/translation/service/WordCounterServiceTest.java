package my.javacraft.translation.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class WordCounterServiceTest {

    @Test
    public void testContainsNonAlphabeticCharacters() {
        WordCounterService wordCounterService = prepareBasicWordCounterService();

        Assertions.assertFalse(wordCounterService.containsNonAlphabeticCharacters("A"));
        Assertions.assertFalse(wordCounterService.containsNonAlphabeticCharacters("apple"));
        Assertions.assertFalse(wordCounterService.containsNonAlphabeticCharacters("banana"));

        Assertions.assertTrue(wordCounterService.containsNonAlphabeticCharacters("ba ba ba"));
        Assertions.assertTrue(wordCounterService.containsNonAlphabeticCharacters("apple1"));
        Assertions.assertTrue(wordCounterService.containsNonAlphabeticCharacters("222"));
        Assertions.assertTrue(wordCounterService.containsNonAlphabeticCharacters("Romeo’s"));
        Assertions.assertTrue(wordCounterService.containsNonAlphabeticCharacters("’"));
        Assertions.assertTrue(wordCounterService.containsNonAlphabeticCharacters("2"));
    }

    @Test
    public void testCorrectSingleWord() {
        String word = "myWord";

        WordCounterService wordCounterService = prepareBasicWordCounterService();
        wordCounterService.addWords(word);

        Assertions.assertEquals(1, wordCounterService.counterByWord(word));
    }

    @Test
    public void testIncorrectSingleWord() {
        String word = "Romeo’s";

        WordCounterService wordCounterService = prepareBasicWordCounterService();
        wordCounterService.addWords(word);

        Assertions.assertEquals(0, wordCounterService.counterByWord(word));
    }

    @Test
    public void testSeveralUniqueWords() {
        String words = "myWordOne, myWordTwo";

        WordCounterService wordCounterService = prepareBasicWordCounterService();
        wordCounterService.addWords(words);

        Assertions.assertEquals(1, wordCounterService.counterByWord("myWordOne"));
        Assertions.assertEquals(1, wordCounterService.counterByWord("myWordTwo"));
    }

    @Test
    public void testSeveralRepeatableWords() {
        String words = "apple, pear, apple, pear";

        WordCounterService wordCounterService = prepareBasicWordCounterService();
        wordCounterService.addWords(words);

        Assertions.assertEquals(2, wordCounterService.counterByWord("apple"));
        Assertions.assertEquals(2, wordCounterService.counterByWord("pear"));
    }

    @Test
    public void testWordsFromOtherLanguages() {
        String words = "flower, flor, blume, bee";

        TranslateService translateService = mock(TranslateService.class);
        when(translateService.translate2English(any())).then(a -> a.getArguments()[0]);
        when(translateService.translate2English(eq("flor"))).thenReturn("flower");
        when(translateService.translate2English(eq("blume"))).thenReturn("flower");

        WordCounterService wordCounterService = new WordCounterService(translateService);
        wordCounterService.addWords(words);

        Assertions.assertEquals(3, wordCounterService.counterByWord("flower"));
        Assertions.assertEquals(1, wordCounterService.counterByWord("bee"));
    }

    @Test
    public void testStrangeCombinations() {
        String words = "    !!! ; 9 8 \\";

        WordCounterService wordCounterService = prepareBasicWordCounterService();
        wordCounterService.addWords(words);

        Assertions.assertEquals(0, wordCounterService.counterByWord(" "));
        Assertions.assertEquals(0, wordCounterService.counterByWord("!!!"));
        Assertions.assertEquals(0, wordCounterService.counterByWord(";"));
        Assertions.assertEquals(0, wordCounterService.counterByWord("9"));
        Assertions.assertEquals(0, wordCounterService.counterByWord("8"));
        Assertions.assertEquals(0, wordCounterService.counterByWord("\\"));
    }

    @Test
    public void testBigText() throws IOException {
        List<String> wordList = getWordsFromTxtFile("src/test/resources/BigText.txt");

        WordCounterService wordCounterService = prepareBasicWordCounterService();
        wordCounterService.addWords(wordList.toString().substring(1, wordList.toString().length() - 1));

        Assertions.assertEquals(4, wordCounterService.counterByWord("Amazon"));
        Assertions.assertEquals(34, wordCounterService.counterByWord("Kinesis"));
        Assertions.assertEquals(14, wordCounterService.counterByWord("Streams"));
    }

    @Test
    public void testRomeoAndJuliet() throws IOException {
        List<String> wordList = getWordsFromTxtFile("src/test/resources/Romeo_and_Juliet.txt");

        WordCounterService wordCounterService = prepareBasicWordCounterService();
        wordCounterService.addWords(wordList.toString().substring(1, wordList.toString().length() - 1));

        Assertions.assertEquals(300, wordCounterService.counterByWord("Romeo"));
        Assertions.assertEquals(180, wordCounterService.counterByWord("Juliet"));
    }

    private List<String> getWordsFromTxtFile(String pathToFile) throws IOException {
        Path path = Paths.get(pathToFile);
        List<String> lineList = Files.readAllLines(path);

        List<String> wordList = new ArrayList<>();
        for (String line : lineList) {
            wordList.addAll(WordCounterService.splitText(line));
        }
        return wordList;
    }

    private WordCounterService prepareBasicWordCounterService() {
        TranslateService translateService = mock(TranslateService.class);
        when(translateService.translate2English(any())).then(a -> a.getArguments()[0]);

        return new WordCounterService(translateService);
    }
}
