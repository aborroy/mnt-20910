package org.alfresco.cmis.assoc.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class WordGenerator
{

    private Random r = new Random();

    @Value("classpath:alice-oz.txt")
    Resource contentFile;

    String[] words;
    private void initWords()
    {
        try
        {
            byte[] bytes = contentFile.getInputStream().readAllBytes();
            words = new String(bytes).trim().split(" ");
        } 
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }
    
    public String markov(int keySize, int outputSize) 
    {
        
        if (words == null) initWords();
        
        if (keySize < 1) throw new IllegalArgumentException("Key size can't be less than 1");
        
        if (outputSize < keySize || outputSize >= words.length) {
            throw new IllegalArgumentException("Output size is out of range");
        }
        Map<String, List<String>> dict = new HashMap<>();
 
        for (int i = 0; i < (words.length - keySize); ++i) {
            StringBuilder key = new StringBuilder(words[i]);
            for (int j = i + 1; j < i + keySize; ++j) {
                key.append(' ').append(words[j]);
            }
            String value = (i + keySize < words.length) ? words[i + keySize] : "";
            if (!dict.containsKey(key.toString())) {
                ArrayList<String> list = new ArrayList<>();
                list.add(value);
                dict.put(key.toString(), list);
            } else {
                dict.get(key.toString()).add(value);
            }
        }
 
        int n = 0;
        int rn = r.nextInt(dict.size());
        String prefix = (String) dict.keySet().toArray()[rn];
        List<String> output = new ArrayList<>(Arrays.asList(prefix.split(" ")));
 
        while (true) {
            List<String> suffix = dict.get(prefix);
            if (suffix == null)
            {
                return "random";
            } 
            else
            {    
                if (suffix.size() == 1) {
                    if (Objects.equals(suffix.get(0), "")) return output.stream().reduce("", (a, b) -> a + " " + b);
                    output.add(suffix.get(0));
                } else {
                    rn = r.nextInt(suffix.size());
                    output.add(suffix.get(rn));
                }
                if (output.size() >= outputSize) return output.stream().limit(outputSize).reduce("", (a, b) -> a + " " + b);
                n++;
                prefix = output.stream().skip(n).limit(keySize).reduce("", (a, b) -> a + " " + b).trim();
            }
        }
    }
    
    public String getSentence()
    {
        String sentence = markov(4, 4).replaceAll("[^a-zA-Z ]", "").trim();
        return sentence;
    }
    
    public String getWord()
    {
        String word = markov(1, 1).replaceAll("[^a-zA-Z ]", "").trim();
        return word;
    }
    
}
