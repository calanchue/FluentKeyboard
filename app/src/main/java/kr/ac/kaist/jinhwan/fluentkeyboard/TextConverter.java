package kr.ac.kaist.jinhwan.fluentkeyboard;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TextConverter {
    // 초성 19자
    static List<Character> l1List = Arrays.asList('ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ');
    static Set<Character> l1 = new HashSet<>(l1List);
    // 중성 21자
    static List<Character> l2List = Arrays.asList('ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ');
    static Set<Character> l2 = new HashSet<>(l2List);
    // 종성 28자 (무받침 포함)
    static List<Character> l3List = Arrays.asList(' ', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ');
    static Set<Character> l3 = new HashSet<>(l3List);



    public static String convertRaw(String text){
        Log.d("ASDF", String.format("%b",l2.contains("ㅣ")));
        Log.v("converter", "input="+text);
        String retext;

        retext = text.replaceAll("ㄱㄱ", "ㄲ");

        retext = retext.replaceAll("ㄷㄷ", "ㄸ");

        retext = retext.replaceAll("ㅈㅈ", "ㅉ");

        retext = retext.replaceAll("ㅂㅂ", "ㅃ");

        retext = retext.replaceAll("ㅅㅅ", "ㅆ");

        retext = retext.replaceAll("ㅣ·", "ㅏ");

        retext = retext.replaceAll("ㅏ·", "ㅑ");

        retext = retext.replaceAll("·ㅣ", "ㅓ");

        retext = retext.replaceAll("·ㅓ", "ㅕ");

        retext = retext.replaceAll("·ㅡ", "ㅗ");

        retext = retext.replaceAll("·ㅗ", "ㅛ");

        retext = retext.replaceAll("ㅡ·", "ㅜ");

        retext = retext.replaceAll("ㅜ·", "ㅠ");

        retext = retext.replaceAll("ㅏㅣ", "ㅐ");

        retext = retext.replaceAll("ㅑㅣ", "ㅒ");

        retext = retext.replaceAll("ㅓㅣ", "ㅔ");

        retext = retext.replaceAll("ㅕㅣ", "ㅖ");

        retext = retext.replaceAll("ㅗㅏ", "ㅘ");

        retext = retext.replaceAll("ㅗㅐ", "ㅙ");

        retext = retext.replaceAll("ㅗㅣ", "ㅚ");

        retext = retext.replaceAll("ㅜㅓ", "ㅝ");

        retext = retext.replaceAll("ㅜㅔ", "ㅞ");

        retext = retext.replaceAll("ㅜㅣ", "ㅟ");

        retext = retext.replaceAll("ㅡㅣ", "ㅢ");

        retext = retext.replaceAll("ㅡㅕ", "ㅝ");

        Log.v("converter", "retext"+retext);
//	retext = retext.replaceAll("", "");
        if(retext.length() <1){
            return "";
        }
        else if(retext.length() <2){
            return ""+retext.charAt(0);
        }

        StringBuilder p_result = new StringBuilder();
        int tmp_num= 0;
        for(int i=0; i<retext.length(); i++) {

            char letter = retext.charAt(i);


            boolean k1 = l1.contains(letter);
            boolean k2 = l2.contains(letter);
            boolean k3 = l3.contains(letter);

            boolean is_next_moeum = false;
            boolean is_next_jaum = false;
            if(i+1 < retext.length()) {
                char next_letter = retext.charAt(i + 1);
                is_next_moeum = l2.contains(next_letter);
                is_next_jaum = l3.contains(next_letter);
            }
            Log.d("converter", String.format("c=%c,%x i=%d k1=%b, k2=%b, k3=%b, moeum=%b, jaum=%b",letter,(int)letter,i,k1,k2,k3,is_next_moeum,is_next_jaum));
            // 초성 (다음에 모음이 오면 초성임)
            if(k1 && is_next_moeum) {
                tmp_num =0xAC00 + 28 * 21*l1List.indexOf(letter);
                Log.v("converter",String.format("k1 tmpNum=%d ,char=%c",tmp_num,(char)tmp_num));
                // 중성
            } else if(k2) {
                tmp_num += l2List.indexOf(letter)*28;
                // 모음 다음에 자음이 오면 어절 종료 체크
                if(!is_next_moeum) {
                    boolean is_next_next_moeum = false;
                    if(i+2 < retext.length()) {
                        is_next_next_moeum = l2.contains(retext.charAt(i + 2));
                    }
                    // 모음/자음/모음인 경우 여기가 어절의 끝
                    // 마지막 단어여도 어절 끝
                    // 다음 단어가 띄어쓰기여도 어절 끝
                    if(is_next_next_moeum || i+1==retext.length() || retext.charAt(i+1)==' ') {

                        p_result.append( (char)tmp_num);
                        Log.v("converter",String.format("k2 tmpNum=%d ,char=%c",tmp_num,(char)tmp_num));
                        tmp_num = 0;
                    }
                }
                // 종성 (다음에 자음이 오면 종성임)
            } else if(k3 && !is_next_moeum) {
                tmp_num += l3List.indexOf(letter);
                // 종성 다음에 자음이 오면 어절의 종료
                if(!is_next_moeum || i+1==retext.length()) {
                    if(tmp_num < 0xAC00){
                        p_result.append(letter);
                        Log.v("converter",String.format("k3 char=%c",letter));
                    }else {
                        p_result.append((char) tmp_num);
                        Log.v("converter",String.format("k3 tmpNum=%d ,char=%c",tmp_num,(char)tmp_num));
                    }
                    tmp_num = 0;
                }
            } else {
                p_result.append(" ");
                tmp_num = 0;
            }

        }
        Log.v("converter", "lasttmpNum:"+tmp_num);
        if(tmp_num != 0){
            p_result.append ((char)tmp_num);
        }

        return p_result.toString();
    }

    private static final char[] CHO =
        /*ㄱ ㄲ ㄴ ㄷ ㄸ ㄹ ㅁ ㅂ ㅃ ㅅ ㅆ ㅇ ㅈ ㅉ ㅊ ㅋ ㅌ ㅍ ㅎ */
            {0x3131, 0x3132, 0x3134, 0x3137, 0x3138, 0x3139, 0x3141, 0x3142, 0x3143, 0x3145,
                    0x3146, 0x3147, 0x3148, 0x3149, 0x314a, 0x314b, 0x314c, 0x314d, 0x314e};
    private static final char[] JUN =
		/*ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ*/
            {0x314f, 0x3150, 0x3151, 0x3152, 0x3153, 0x3154, 0x3155, 0x3156, 0x3157, 0x3158,
                    0x3159, 0x315a, 0x315b, 0x315c, 0x315d, 0x315e, 0x315f, 0x3160, 0x3161, 0x3162,
                    0x3163};
    /*X ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ*/
    private static final char[] JON =
            {0x0000, 0x3131, 0x3132, 0x3133, 0x3134, 0x3135, 0x3136, 0x3137, 0x3139, 0x313a,
                    0x313b, 0x313c, 0x313d, 0x313e, 0x313f, 0x3140, 0x3141, 0x3142, 0x3144, 0x3145,
                    0x3146, 0x3147, 0x3148, 0x314a, 0x314b, 0x314c, 0x314d, 0x314e};




}
