package main.printtokens;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

public class printtokens_tests {
    
    private Printtokens printtokens;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    
    @BeforeEach
    void setUp() {
        printtokens = new Printtokens();
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }
    
    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }
    
    // Helper method to create temporary test files
    private File createTempFile(String content) throws IOException {
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }
        return tempFile;
    }
    

    @Test
    void testOpenCharacterStreamWithValidFile() throws IOException {
        File testFile = createTempFile("test content");
        
        BufferedReader br = printtokens.open_character_stream(testFile.getAbsolutePath());
        assertNotNull(br);
        

        String line = br.readLine();
        assertEquals("test content", line);
        br.close();
    }
    
    @Test
    public void testOpenCharacterStreamWithInvalidFile() {
        BufferedReader reader = printtokens.open_character_stream("nonexistentfile.txt");
        assertNull(reader, "Expected null reader for nonexistent file");
    }

    @Test
    void testOpenCharacterStreamWithNull() {
 
        BufferedReader br = printtokens.open_character_stream(null);
        
        
        assertNotNull(br, "BufferedReader should not be null when fname is null");
        
       try {
            br.close();
        } catch (IOException e) {
            
        }
    }
    

    @Test
    void testGetChar() throws IOException {
        String testContent = "abc";
        BufferedReader br = new BufferedReader(new StringReader(testContent));
        
        assertEquals('a', printtokens.get_char(br));
        assertEquals('b', printtokens.get_char(br));
        assertEquals('c', printtokens.get_char(br));
        assertEquals(-1, printtokens.get_char(br)); // EOF
        
        br.close();
    }
    
    @Test
    public void testGetCharThrowsIOException() {
        Printtokens pt = new Printtokens();

       
        BufferedReader br = new BufferedReader(new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("Forced exception for test coverage.");
            }

            @Override
            public void close() throws IOException {
                
            }
        });

        
        int result = pt.get_char(br);

        
        assertEquals(0, result);
    }
    

    
    
    @Test
    void testUngetChar() throws IOException {
        String testContent = "ab";
        BufferedReader br = new BufferedReader(new StringReader(testContent));
        
        int ch = printtokens.get_char(br);
        assertEquals('a', ch);
        
        printtokens.unget_char(ch, br);
        
        assertEquals('a', printtokens.get_char(br));
        
        br.close();
    }
    
    
    @Test
    void testOpenTokenStreamWithNull() {
        BufferedReader br = printtokens.open_token_stream(null);
        assertNotNull(br);
    }
    
    @Test
    void testOpenTokenStreamWithEmptyString() {
        BufferedReader br = printtokens.open_token_stream("");
        assertNotNull(br);
    }
    
    @Test
    void testOpenTokenStreamWithValidFile() throws IOException {
        File testFile = createTempFile("test tokens");
        
        BufferedReader br = printtokens.open_token_stream(testFile.getAbsolutePath());
        assertNotNull(br);
        br.close();
    }
    
  
    @Test
    void testGetTokenBasicTokens() throws IOException {
        String input = "hello world 123";
        BufferedReader br = new BufferedReader(new StringReader(input));
        
        assertEquals("hello", printtokens.get_token(br));
        assertEquals("world", printtokens.get_token(br));
        assertEquals("123", printtokens.get_token(br));
        assertNull(printtokens.get_token(br)); 
        
        br.close();
    }
    
    @Test
    void testGetTokenWithSpecialSymbols() throws IOException {
        String input = "( ) [ ] , ` '";
        BufferedReader br = new BufferedReader(new StringReader(input));
        
        assertEquals("(", printtokens.get_token(br));
        assertEquals(")", printtokens.get_token(br));
        assertEquals("[", printtokens.get_token(br));
        assertEquals("]", printtokens.get_token(br));
        assertEquals(",", printtokens.get_token(br));
        assertEquals("`", printtokens.get_token(br));
        assertEquals("'", printtokens.get_token(br));
        
        br.close();
    }
    
    @Test
    void testGetTokenWithString() throws IOException {
        String input = "\"hello world\"";
        BufferedReader br = new BufferedReader(new StringReader(input));
        
        assertEquals("\"hello world\"", printtokens.get_token(br));
        
        br.close();
    }
    
    @Test
    void testGetTokenWithComment() throws IOException {
        String input = ";this is a comment\n";
        BufferedReader br = new BufferedReader(new StringReader(input));
        
        assertEquals(";this is a comment", printtokens.get_token(br));
        
        br.close();
    }
    
    @Test
    void testGetTokenWithWhitespace() throws IOException {
        String input = "  \n\r  token  \n\r  ";
        BufferedReader br = new BufferedReader(new StringReader(input));
        
        assertEquals("token", printtokens.get_token(br));
        assertNull(printtokens.get_token(br));
        
        br.close();
    }
    
    @Test
    public void testGetTokenStartsWithSemicolonInDefaultState() throws IOException {
       
        String input = ";comment";
        BufferedReader br = new BufferedReader(new StringReader(input));

        
        Printtokens pt = new Printtokens();  
        String token = pt.get_token(br);     

        
        assertEquals(";comment", token);
    }
    
   
    @Test
    void testIsTokenEndWithEOF() {
        assertTrue(Printtokens.is_token_end(0, -1));
    }
    
    @Test
    void testIsTokenEndWithString() {
        assertTrue(Printtokens.is_token_end(1, '"'));
        assertTrue(Printtokens.is_token_end(1, '\n'));
        assertTrue(Printtokens.is_token_end(1, '\r'));
        assertTrue(Printtokens.is_token_end(1, '\t'));
        assertFalse(Printtokens.is_token_end(1, 'a'));
    }
    
    @Test
    void testIsTokenEndWithComment() {
        assertTrue(Printtokens.is_token_end(2, '\n'));
        assertTrue(Printtokens.is_token_end(2, '\r'));
        assertTrue(Printtokens.is_token_end(2, '\t'));
        assertFalse(Printtokens.is_token_end(2, 'a'));
    }
    
    @Test
    void testIsTokenEndWithSpecialSymbols() {
        assertTrue(Printtokens.is_token_end(0, '('));
        assertTrue(Printtokens.is_token_end(0, ')'));
        assertTrue(Printtokens.is_token_end(0, ' '));
        assertTrue(Printtokens.is_token_end(0, 59)); 
    }
    
    @Test
    void testIsTokenEndContinueToken() {
        
        assertFalse(Printtokens.is_token_end(0, 'a'));  
    }
    
    
    @Test
    void testTokenTypeKeyword() {
        assertEquals(Printtokens.keyword, Printtokens.token_type("and"));
        assertEquals(Printtokens.keyword, Printtokens.token_type("or"));
        assertEquals(Printtokens.keyword, Printtokens.token_type("if"));
        assertEquals(Printtokens.keyword, Printtokens.token_type("xor"));
        assertEquals(Printtokens.keyword, Printtokens.token_type("lambda"));
        assertEquals(Printtokens.keyword, Printtokens.token_type("=>"));
    }
    
    @Test
    void testTokenTypeSpecSymbol() {
        assertEquals(Printtokens.spec_symbol, Printtokens.token_type("("));
        assertEquals(Printtokens.spec_symbol, Printtokens.token_type(")"));
        assertEquals(Printtokens.spec_symbol, Printtokens.token_type("["));
        assertEquals(Printtokens.spec_symbol, Printtokens.token_type("]"));
        assertEquals(Printtokens.spec_symbol, Printtokens.token_type(","));
        assertEquals(Printtokens.spec_symbol, Printtokens.token_type("`"));
        assertEquals(Printtokens.spec_symbol, Printtokens.token_type("'"));
    }
    @Test
    void testTokenTypeErrorSymbols() {
        assertEquals(Printtokens.error, Printtokens.token_type("@"));
        assertEquals(Printtokens.error, Printtokens.token_type("#"));
        assertEquals(Printtokens.error, Printtokens.token_type("!"));
        assertEquals(Printtokens.error, Printtokens.token_type("$"));
        assertEquals(Printtokens.error, Printtokens.token_type("%"));
    }
    @Test
    void testTokenTypeOnlyCommentBranch() {
        
        String input = ";justAComment";
        assertEquals(Printtokens.comment, Printtokens.token_type(input));
    }


    
    @Test
    void testTokenTypeIdentifier() {
        assertEquals(Printtokens.identifier, Printtokens.token_type("variable"));
        assertEquals(Printtokens.identifier, Printtokens.token_type("test123"));
    }
    
    @Test
    void testTokenTypeNumConstant() {
        assertEquals(Printtokens.num_constant, Printtokens.token_type("1"));  
        assertEquals(Printtokens.num_constant, Printtokens.token_type("0"));  
        assertEquals(Printtokens.num_constant, Printtokens.token_type("22"));
    }
    
    @Test
    void testTokenTypeStrConstant() {
        assertEquals(Printtokens.str_constant, Printtokens.token_type("\"hello\""));
        assertEquals(Printtokens.str_constant, Printtokens.token_type("\"\""));
    }
    
    @Test
    void testTokenTypeCharConstant() {
        assertEquals(Printtokens.char_constant, Printtokens.token_type("#a"));
        assertEquals(Printtokens.char_constant, Printtokens.token_type("#z"));
    }
    
    @Test
    public void testTokenTypeEmptyString() {
        assertThrows(StringIndexOutOfBoundsException.class, () -> {
            Printtokens.token_type("");
        });
    }
    
    @Test
    public void testTokenTypeNullInput() {
        
        assertThrows(NullPointerException.class, () -> {
            Printtokens.token_type(null);
        });
    }
    
    @Test
    void testTokenTypeInvalidSymbols() {
        assertEquals(Printtokens.error, Printtokens.token_type("@"));
        assertEquals(Printtokens.error, Printtokens.token_type("#"));
        assertEquals(Printtokens.error, Printtokens.token_type("!"));
    }



    @Test
    void testTokenTypeComment() {
        assertEquals(Printtokens.comment, Printtokens.token_type(";comment"));
    }
    
    @Test
    void testTokenTypeError() {
        assertEquals(Printtokens.error, Printtokens.token_type("@invalid"));
    }
    
    
    @Test
    void testPrintTokenKeyword() {
        printtokens.print_token("and");
        assertTrue(outputStream.toString().contains("keyword,\"and\"."));
    }
    
    @Test
    void testPrintTokenSpecSymbol() {
        printtokens.print_token("(");
        assertTrue(outputStream.toString().contains("lparen."));
    }
    
    @Test
    void testPrintTokenIdentifier() {
        printtokens.print_token("variable");
        assertTrue(outputStream.toString().contains("identifier,\"variable\"."));
    }
    
    @Test
    void testPrintTokenNumConstant() {
        printtokens.print_token("1");  
        assertTrue(outputStream.toString().contains("numeric,1."));
    }
    
    @Test
    void testPrintTokenStrConstant() {
        printtokens.print_token("\"hello\"");
        assertTrue(outputStream.toString().contains("string,\"hello\"."));
    }
    
    @Test
    void testPrintTokenCharConstant() {
        printtokens.print_token("#a");
        assertTrue(outputStream.toString().contains("character,\"a\"."));
    }
    
    @Test
    void testPrintTokenComment() {
        printtokens.print_token(";comment");
        assertTrue(outputStream.toString().contains("comment,\";comment\"."));
    }
    
    @Test
    void testPrintTokenError() {
        printtokens.print_token("@invalid");
        assertTrue(outputStream.toString().contains("error,\"@invalid\"."));
    }
    
    
    @Test
    void testIsComment() {
        assertTrue(Printtokens.is_comment(";comment"));
        assertFalse(Printtokens.is_comment("not_comment"));
    }
    
    @Test
    void testIsKeyword() {
        assertTrue(Printtokens.is_keyword("and"));
        assertTrue(Printtokens.is_keyword("or"));
        assertTrue(Printtokens.is_keyword("if"));
        assertTrue(Printtokens.is_keyword("xor"));
        assertTrue(Printtokens.is_keyword("lambda"));
        assertTrue(Printtokens.is_keyword("=>"));
        assertFalse(Printtokens.is_keyword("not_keyword"));
    }
    
    @Test
    void testIsCharConstant() {
        assertTrue(Printtokens.is_char_constant("#a"));
        assertTrue(Printtokens.is_char_constant("#Z"));
        assertFalse(Printtokens.is_char_constant("abc")); 
        assertFalse(Printtokens.is_char_constant("ab"));
        assertFalse(Printtokens.is_char_constant("#1")); 
    }
    
    @Test
    void testIsNumConstant() {
        assertTrue(Printtokens.is_num_constant("1"));     
        assertTrue(Printtokens.is_num_constant("0"));     
        assertFalse(Printtokens.is_num_constant("abc"));
    }
    
    @Test
    public void testIsStrConstant() {
        assertTrue(Printtokens.is_str_constant("\"valid\""));
        assertFalse(Printtokens.is_str_constant("not a string"));
        assertFalse(Printtokens.is_str_constant("\"unterminated"));
        assertTrue(Printtokens.is_str_constant("\"\""));
    }

    
    @Test
    public void testIsIdentifier() {
        assertTrue(Printtokens.is_identifier("validVar"));
        assertFalse(Printtokens.is_identifier("123abc")); 
    }

    
    @Test
    void testGetTokenSemicolonOnly() throws IOException {
        String input = ";";
        BufferedReader br = new BufferedReader(new StringReader(input));
        assertEquals(";", printtokens.get_token(br));
        assertNull(printtokens.get_token(br)); 
        br.close();
    }
    
    @Test
    void testGetTokenSpecSymbolThenWord() throws IOException {
        String input = "(word)";
        BufferedReader br = new BufferedReader(new StringReader(input));
        assertEquals("(", printtokens.get_token(br));
        assertEquals("word", printtokens.get_token(br));
        assertEquals(")", printtokens.get_token(br));
        br.close();
    }
    
    @Test
    void testGetTokenSpecSymbolEOF() throws IOException {
        String input = "(";
        BufferedReader br = new BufferedReader(new StringReader(input));
        assertEquals("(", printtokens.get_token(br));
        assertNull(printtokens.get_token(br));
        br.close();
    }

    @Test
    void testGetTokenInvalidSymbolToken() throws IOException {
        String input = "@invalid";
        BufferedReader br = new BufferedReader(new StringReader(input));
        assertEquals("@invalid", printtokens.get_token(br));
        br.close();
    }
    @Test
    void testWordFollowedBySpecSymbol() throws IOException {
        String input = "abc)";
        BufferedReader br = new BufferedReader(new StringReader(input));
        assertEquals("abc", printtokens.get_token(br));
        assertEquals(")", printtokens.get_token(br));
        br.close();
    }
    
    @Test
    void testGetTokenWordFollowedBySemicolon() throws IOException {
        String input = "word;";
        BufferedReader br = new BufferedReader(new StringReader(input));

        
        assertEquals("word", printtokens.get_token(br));
        assertEquals(";", printtokens.get_token(br));
        
        br.close();
    }
    
    @Test
    void testGetTokenTerminatedString() throws IOException {
        String input = "\"proper string\"";
        BufferedReader br = new BufferedReader(new StringReader(input));
        assertEquals("\"proper string\"", printtokens.get_token(br));
        br.close();
    }






    
    @Test
    void testIsSpecSymbol() {
        assertTrue(Printtokens.is_spec_symbol('('));
        assertTrue(Printtokens.is_spec_symbol(')'));
        assertTrue(Printtokens.is_spec_symbol('['));
        assertTrue(Printtokens.is_spec_symbol(']'));
        assertTrue(Printtokens.is_spec_symbol('\''));
        assertTrue(Printtokens.is_spec_symbol('`'));
        assertTrue(Printtokens.is_spec_symbol(','));
        assertFalse(Printtokens.is_spec_symbol('a'));
        assertFalse(Printtokens.is_spec_symbol('1'));
    }
    
    
    @Test
    void testFullTokenization() throws IOException {
        String input = "and (test) \"string\" 123 ;comment\n#a => [list]";
        BufferedReader br = new BufferedReader(new StringReader(input));
        
        
        assertEquals("and", printtokens.get_token(br));
        assertEquals("(", printtokens.get_token(br));
        assertEquals("test", printtokens.get_token(br));
        assertEquals(")", printtokens.get_token(br));
        assertEquals("\"string\"", printtokens.get_token(br));
        assertEquals("123", printtokens.get_token(br));
        assertEquals(";comment", printtokens.get_token(br));
        assertEquals("#a", printtokens.get_token(br));
        assertEquals("=>", printtokens.get_token(br));
        assertEquals("[", printtokens.get_token(br));
        assertEquals("list", printtokens.get_token(br));
        assertEquals("]", printtokens.get_token(br));
        assertNull(printtokens.get_token(br));
        
        br.close();
    }
    
    
    @Test
    void testEmptyInput() throws IOException {
        BufferedReader br = new BufferedReader(new StringReader(""));
        assertNull(printtokens.get_token(br));
        br.close();
    }
    
    @Test
    void testOnlyWhitespace() throws IOException {
        BufferedReader br = new BufferedReader(new StringReader("   \n\r   "));
        assertNull(printtokens.get_token(br));
        br.close();
    }
    
    @Test
    void testUnterminatedString() throws IOException {
        String input = "\"unterminated string\n";
        BufferedReader br = new BufferedReader(new StringReader(input));
        
        String token = printtokens.get_token(br);
        assertEquals("\"unterminated string", token);
        
        br.close();
    }
    
    @Test
    void testSingleCharacterTokens() throws IOException {
        String input = "a 1 (";
        BufferedReader br = new BufferedReader(new StringReader(input));
        
        assertEquals("a", printtokens.get_token(br));
        assertEquals("1", printtokens.get_token(br));
        assertEquals("(", printtokens.get_token(br));
        
        br.close();
    }
    
    @Test
    public void testPrintSpecSymbolAllCases() {
        Printtokens.print_spec_symbol("(");   
        Printtokens.print_spec_symbol(")");   
        Printtokens.print_spec_symbol("[");   
        Printtokens.print_spec_symbol("]");   
        Printtokens.print_spec_symbol("\"");  
        Printtokens.print_spec_symbol("`");   
        Printtokens.print_spec_symbol(",");   
        Printtokens.print_spec_symbol("@");   
    }
    @Test
    public void testPrintSpecSymbolQuote() {
        Printtokens.print_spec_symbol("'");
        assertTrue(outputStream.toString().contains("quote."));
    }
    @Test
    public void testPrintSpecSymbolUnknown() {
        Printtokens.print_spec_symbol("@");
        assertFalse(outputStream.toString().contains("."));
    }
    @Test
    void testPrintTokenSpecSymbolQuote() {
        printtokens.print_token("'");
        assertTrue(outputStream.toString().contains("quote."));
    }





    @Test
    public void testIsNumConstantExtraCases() {
        assertTrue(Printtokens.is_num_constant("123"));
        assertTrue(Printtokens.is_num_constant("1"));
        assertFalse(Printtokens.is_num_constant("1a"));
        assertFalse(Printtokens.is_num_constant("a12"));
        
    }
    
    @Test
    public void testIsNumConstantEmptyString() {
        assertThrows(StringIndexOutOfBoundsException.class, () -> {
            Printtokens.is_num_constant("");
        });
    }

    @Test
    public void testIsNumConstantNullInput() {
        assertThrows(NullPointerException.class, () -> {
            Printtokens.is_num_constant(null);
        });
    }

    @Test
    public void testPrintTokenExtraCases() {
        Printtokens pt = new Printtokens();
        pt.print_token(";this is a comment");  
        pt.print_token("`");                   
    }

    @Test
    public void testGetChar_EOF() throws IOException {
        Printtokens pt = new Printtokens();
        BufferedReader br = new BufferedReader(new StringReader(""));
        assertEquals(-1, pt.get_char(br));  
    }

    @Test
    public void testUngetChar_MarkUnsupported() throws IOException {
        Printtokens pt = new Printtokens();
        Reader unsupported = new Reader() {
            public int read(char[] cbuf, int off, int len) { return -1; }
            public void close() {}
        };
        BufferedReader br = new BufferedReader(unsupported);
        pt.unget_char(97, br); 
    }

    @Test
    public void testTokenType_ErrorCase() {
        assertEquals(0, Printtokens.token_type("@invalid!")); 
    }

    @Test
    public void testIsIdentifierValid() {
        assertTrue(Printtokens.is_identifier("abc123")); 
    }



    
    @Test
    public void testIsIdentifierInvalid() {
        assertFalse(Printtokens.is_identifier("a#")); 
    }
    
        
}