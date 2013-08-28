/**
 * 
 */
package CSSHelper;

import java.util.HashSet;

/**
 * @author Davood Mazinanian
 *
 */
public class ListStyleHelper {
	// http://www.w3.org/TR/css3-lists/
	@SuppressWarnings("serial")
	private static HashSet<String> styleTypeNames = new HashSet<String>() {{
	add("persian");							add("persian-alphabetic");				add("persian-abjad");	
	add("greek");							add("lower-greek");						add("korean-hanja-formal");
	add("korean-hanja-informal");			add("korean-hangul-formal");			add("japanese-formal");
	add("japanese-informal");				add("ancient-tamil");					add("georgian");
	add("armenian");						add("upper-armenian"); 					add("lower-armenian");
	add("lower-roman");						add("upper-roman");						add("simple-lower-roman");
	add("simple-upper-roman");				add("hebrew");							add("parenthesized-hangul-syllable");
	add("parenthesized-hangul-consonants");	add("parenthesized-lower-latin");		add("parenthesized-decimal");			
	add("fullwidth-lower-roman");			add("fullwidth-upper-roman");			add("filled-circled-decimal");			
	add("double-circled-decimal");			add("dotted-decimal");					add("decimal-leading-zero");
	add("circled-korean-syllables");		add("circled-korean-consonants");		add("circled-upper-latin");				
	add("circled-lower-latin");				add("circled-decimal");					add("upper-alpha-symbolic");			
	add("lower-alpha-symbolic");			add("footnotes");						add("asterisks");	
	add("yemsa");							add("wolaita"); 						add("upper-ukrainian-full");
	add("upper-ukrainian");					add("upper-serbo-croatian");			add("upper-russian-full");			
	add("upper-russian");					add("upper-oromo-qubee");				add("upper-macedonian");	
	add("upper-bulgarian");					add("upper-belorussian");				add("upper-alpha");				
	add("tigre");							add("thai-alphabetic");					add("silti");		
	add("sidama");							add("saho");							add("oromo");	
	add("meen");							add("lower-ukrainian-full");			add("lower-ukrainian");
	add("lower-serbo-croatian");			add("lower-russian-full");				add("lower-russian");		
	add("lower-oromo-qubee");				add("lower-macedonian");				add("lower-bulgarian");				
	add("lower-belorussian");				add("lower-alpha");						add("kunama");							
	add("korean-syllable ");				add("korean-consonant");				add("konso");	
	add("kembata");							add("kebena");							add("katakana");	
	add("katakana-iroha");					add("kaffa");							add("hiragana");		
	add("hiragana-iroha");					add("hindi");							add("harari");						
	add("hadiyya");							add("gumuz");							add("gedeo");			
	add("fullwidth-upper-alpha");			add("fullwidth-lower-alpha");			add("dizi");
	add("cjk-heavenly-stem");				add("cjk-earthly-branch");				add("blin");	
	add("ari");								add("agaw");							add("afar");				
	add("upper-hexadecimal");				add("thai");							add("tibetan");							
	add("telugu");							add("tamil");							add("super-decimal");		
	add("oriya");							add("octal");							add("new-base-60");	
	add("myanmar");							add("mongolian");						add("marathi");			
	add("malayalam");						add("lepcha");							add("lao");	
	add("lower-hexadecimal");				add("khmer");							add("kannada");	
	add("gurmukhi");						add("gujarati");						add("fullwidth-decimal");	
	add("eastern-nagari");					add("devanagari");						add("decimal");			
	add("cjk-decimal");						add("cambodian");						add("burmese");			
	add("binary");							add("bengali");							add("arabic-indic");		
	add("square");							add("dash");							add("disc");				
	add("diamond");							add("circle");							add("check");	
	add("box");
	
	}};
	
	public static boolean isStyleTypeName(String typeName) {
		return styleTypeNames.contains(typeName);
	}
}
