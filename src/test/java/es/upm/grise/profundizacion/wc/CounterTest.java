package es.upm.grise.profundizacion.wc;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class CounterTest {

    @Test
    public void testCountCharactersWordsAndLines() throws IOException {
        String content = "Esta frase\nes un ejemplo para\nel test de recuento.\n";
        BufferedReader reader = new BufferedReader(new StringReader(content));
        
        Counter counter = new Counter(reader);
        
        assertEquals(51, counter.getNumberCharacters());
        assertEquals(3, counter.getNumberLines());
        assertEquals(10, counter.getNumberWords());
    }

    @Test
    public void testEmptyInput() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(""));
        Counter counter = new Counter(reader);
        assertEquals(0, counter.getNumberCharacters());
        assertEquals(0, counter.getNumberLines());
        assertEquals(0, counter.getNumberWords());
    }

    @Test
    public void testSingleCharacterNoSeparator() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader("x"));
        Counter counter = new Counter(reader);
        assertEquals(1, counter.getNumberCharacters());
        assertEquals(0, counter.getNumberLines());
        assertEquals(0, counter.getNumberWords());
    }

    @Test
    public void testEndsWithSpaceCountsWord() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader("hello "));
        Counter counter = new Counter(reader);
        // 'h','e','l','l','o',' ' => 6 characters; space is counted as a word boundary
        assertEquals(6, counter.getNumberCharacters());
        assertEquals(0, counter.getNumberLines());
        assertEquals(1, counter.getNumberWords());
    }

    @Test
    public void testEndsWithoutSeparatorDoesNotCountLastWord() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader("hello"));
        Counter counter = new Counter(reader);
        // 'h','e','l','l','o' => 5 characters; no separator => last word not counted
        assertEquals(5, counter.getNumberCharacters());
        assertEquals(0, counter.getNumberLines());
        assertEquals(0, counter.getNumberWords());
    }

    @Test
    public void testSpacesTabsAndNewlinesAndMultipleSeparators() throws IOException {
        // "one two\tthree\nfour  "
        String content = "one two\tthree\nfour  ";
        BufferedReader reader = new BufferedReader(new StringReader(content));
        Counter counter = new Counter(reader);
        // characters: length including separators = 20
        assertEquals(20, counter.getNumberCharacters());
        // there is one '\n'
        assertEquals(1, counter.getNumberLines());
        // separators: space after "one" (1), tab after "two" (1), newline after "three" (1), two spaces after "four" (2) => 5
        assertEquals(5, counter.getNumberWords());
    }

    @Test
    public void testOnlyNewlines() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader("\n\n"));
        Counter counter = new Counter(reader);
        // two newline characters
        assertEquals(2, counter.getNumberCharacters());
        // each newline increments lines
        assertEquals(2, counter.getNumberLines());
        // each newline is also treated as a word separator (counts as word)
        assertEquals(2, counter.getNumberWords());
    }
}

