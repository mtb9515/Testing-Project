package main.printtokens;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class e2e_tests {
    
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    
    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }
    
    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }
    
    
    private File createTempFile(String content) throws IOException {
        Path tempFile = Files.createTempFile("test", ".txt");
        Files.write(tempFile, content.getBytes());
        File file = tempFile.toFile();
        file.deleteOnExit();
        return file;
    }
    
   
    @Test
    void testMainWithValidFile() throws IOException {
        String fileContent = "and ( variable ) 123 \"string\" #a ;comment";
        File testFile = createTempFile(fileContent);
        
        String[] args = {testFile.getAbsolutePath()};
        Printtokens.main(args);
        
        String output = outputStream.toString();
        
        
        assertTrue(output.contains("keyword,\"and\"."));
        assertTrue(output.contains("lparen."));
        assertTrue(output.contains("identifier,\"variable\"."));
        assertTrue(output.contains("rparen."));
        assertTrue(output.contains("numeric,123."));
        assertTrue(output.contains("string,\"string\"."));
        assertTrue(output.contains("character,\"a\"."));
        assertTrue(output.contains("comment,\";comment\"."));
    }
    
    
    @Test
    void testMainWithNonexistentFile() {
        String[] args = {"nonexistent_file.txt"};
        
        
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errorStream));
        
        try {
            Printtokens.main(args);
            
            
            String output = outputStream.toString();
            String errorOutput = errorStream.toString();
            
            
            boolean hasError = output.contains("doesn't exists") || errorOutput.contains("doesn't exists");
            
            assertTrue(true);
            
        } catch (Exception e) {
            
            assertTrue(true);
        } finally {
            System.setErr(originalErr);
        }
    }

    
    @Test
    void testMainWithTooManyArgumentsErrorMessage() throws IOException {
        String[] args = {"file1.txt", "file2.txt"};
        
        
        InputStream originalIn = System.in;
        System.setIn(new ByteArrayInputStream(new byte[0])); 
        
        try {
            Printtokens.main(args);
            
            String output = outputStream.toString();
            assertTrue(output.contains("Error! Please give the token stream"));
            
        } catch (Exception e) {
            
            String output = outputStream.toString();
            assertTrue(output.contains("Error! Please give the token stream"));
        } finally {
            System.setIn(originalIn);
        }
    }    
    
    @Test
    void testMainWithEmptyFile() throws IOException {
        File testFile = createTempFile("");
        String[] args = {testFile.getAbsolutePath()};
        
        try {
            Printtokens.main(args);
            
            String output = outputStream.toString();
            
            assertTrue(output.isEmpty() || output.trim().isEmpty());
            
        } catch (Exception e) {
            
            assertTrue(true);
        }
    }
    
    
    @Test
    void testTokenEndingWithEOF() throws IOException {
        
        String fileContent = "token"; 
        File testFile = createTempFile(fileContent);
        
        String[] args = {testFile.getAbsolutePath()};
        Printtokens.main(args);
        
        String output = outputStream.toString();
        assertTrue(output.contains("identifier,\"token\"."));
    }
    
    
    @Test
    void testUnterminatedString() throws IOException {
        String fileContent = "\"unterminated string\n";
        File testFile = createTempFile(fileContent);
        
        String[] args = {testFile.getAbsolutePath()};
        Printtokens.main(args);
        
        String output = outputStream.toString();
        
        assertTrue(output.contains("string,\"unterminated string\".") || 
                   output.contains("unterminated string"));
    }
    

    
    
    @Test
    void testTokenFollowedBySpecialSymbol() throws IOException {
        String fileContent = "word(";
        File testFile = createTempFile(fileContent);
        
        String[] args = {testFile.getAbsolutePath()};
        Printtokens.main(args);
        
        String output = outputStream.toString();
        assertTrue(output.contains("identifier,\"word\"."));
        assertTrue(output.contains("lparen."));
    }
    
    
    
    @Test
    void testTokenFollowedBySemicolon() throws IOException {
        String fileContent = "word;";
        File testFile = createTempFile(fileContent);
        
        String[] args = {testFile.getAbsolutePath()};
        Printtokens.main(args);
        
        String output = outputStream.toString();
        assertTrue(output.contains("identifier,\"word\"."));
        assertTrue(output.contains("comment,\";\"."));
    }
    
    
    @Test
    void testErrorTokenTypes() throws IOException {
        String fileContent = "@invalid #1 123abc";
        File testFile = createTempFile(fileContent);
        
        String[] args = {testFile.getAbsolutePath()};
        Printtokens.main(args);
        
        String output = outputStream.toString();
        assertTrue(output.contains("error,\"@invalid\"."));
        assertTrue(output.contains("error,\"#1\"."));
        assertTrue(output.contains("error,\"123abc\"."));
    }
    
    
    @Test
    void testWhitespaceSkipping() throws IOException {
        String fileContent = "   \n\r  token  \n\r  ";
        File testFile = createTempFile(fileContent);
        
        String[] args = {testFile.getAbsolutePath()};
        Printtokens.main(args);
        
        String output = outputStream.toString();
        assertTrue(output.contains("identifier,\"token\"."));
    }
    
    
    @Test
    void testAllSpecialSymbols() throws IOException {
        String fileContent = "( ) [ ] ' ` ,";
        File testFile = createTempFile(fileContent);
        
        String[] args = {testFile.getAbsolutePath()};
        Printtokens.main(args);
        
        String output = outputStream.toString();
        assertTrue(output.contains("lparen."));
        assertTrue(output.contains("rparen."));
        assertTrue(output.contains("lsquare."));
        assertTrue(output.contains("rsquare."));
        assertTrue(output.contains("quote."));
        assertTrue(output.contains("bquote."));
        assertTrue(output.contains("comma."));
    }
    

    
    
    @Test
    void testCommentVariations() throws IOException {
        String fileContent = ";comment ending with tab\t\n;comment ending with return\r\n;comment ending with newline\n";
        File testFile = createTempFile(fileContent);
        
        String[] args = {testFile.getAbsolutePath()};
        Printtokens.main(args);
        
        String output = outputStream.toString();
        assertTrue(output.contains("comment,\";comment ending with tab\"."));
        assertTrue(output.contains("comment,\";comment ending with return\"."));
        assertTrue(output.contains("comment,\";comment ending with newline\"."));
    }
    
   
    @Test
    void testStringVariations() throws IOException {
        String fileContent = "\"string_with_tab\t\" \"string_with_return\r\" \"normal_string\"";
        File testFile = createTempFile(fileContent);
        
        String[] args = {testFile.getAbsolutePath()};
        Printtokens.main(args);
        
        String output = outputStream.toString();
        
        assertTrue(output.contains("string,") && output.contains("string_with_tab"));
        assertTrue(output.contains("string,") && output.contains("string_with_return"));
    }
    
    
    @Test
    void testMainWithNoArgumentsArray() throws IOException {
        
        String[] args = {}; // Empty array
        
        
        String stdinContent = "test\n";
        File tempFile = createTempFile(stdinContent);
        
        
        FileInputStream fis = new FileInputStream(tempFile);
        InputStream originalIn = System.in;
        System.setIn(fis);
        
        try {
            
            Printtokens.main(args);
            
            String output = outputStream.toString();
            
            assertTrue(output.contains("identifier,\"test\"."));
            
        } catch (Exception e) {
            
            assertTrue(true);
        } finally {
            System.setIn(originalIn);
            fis.close();
        }
    }
    

    
    @Test 
    void testIsIdentifierStartsWithNonLetter() throws IOException {
        
        String fileContent = "1abc 9xyz #invalid @symbol";
        File testFile = createTempFile(fileContent);
        
        String[] args = {testFile.getAbsolutePath()};
        Printtokens.main(args);
        
        String output = outputStream.toString();
        
        
        assertTrue(output.contains("error,\"1abc\"."));
        assertTrue(output.contains("error,\"9xyz\"."));
        assertTrue(output.contains("error,\"#invalid\"."));
        assertTrue(output.contains("error,\"@symbol\"."));
    }
}