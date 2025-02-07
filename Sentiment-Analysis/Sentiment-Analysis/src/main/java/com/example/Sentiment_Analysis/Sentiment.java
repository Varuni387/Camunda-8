package com.example.Sentiment_Analysis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class Sentiment {

    private final ZeebeClient zeebeClient;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;
    private static final String OLLAMA_API_URL = "http://localhost:11434/api/generate";

    @Autowired
    public Sentiment(ZeebeClient zeebeClient, ObjectMapper objectMapper) {
        this.zeebeClient = zeebeClient;
        this.objectMapper = objectMapper;
    }

    @JobWorker(type = "analyse")
    public void executeLlamaJob(JobClient jobClient, ActivatedJob job) throws JsonProcessingException {
        System.out.println("Inside sentiment analysis function");

        String prompt = "I will give you a sentence and you MUST return a JSON object that strictly adheres to the following format:\n" +
                "{\n" +
                "  \"sentiment_analysis\": {\n" +
                "    \"text\": \"input_sentence\",\n" +
                "    \"sentiment\": \"positive/negative/neutral\",\n" +
                "    \"score\": sentiment_score,   (float)\n" +
                "    \"category\": \"happy/sad/angry/etc.\",\n" +
                "    \"keywords\": [\"keyword1\", \"keyword2\"],   (array)\n" +
                "    \"language\": \"text_language\",\n" +
                "    \"toxicity\": toxicity_score,   (float)\n" +
                "    \"subjectivity\": subjectivity_score,   (float)\n" +
                "    \"timestamp\": \"2025-02-07T12:34:56Z\"   (ISO 8601 format)\n" +
                "  }\n" +
                "}\n" +
                "Now, here is the sentence:\n" +
                "\"The ongoing Maha Kumbh Mela has seen a massive influx of over 39 crore devotees participating " +
                "in the sacred rituals, with cultural programs resuming to showcase India's diverse heritage. " +
                "High-profile dignitaries are also visiting the event. However, the Mela is also facing challenges, " +
                "including an investigation into a tragic stampede on Mauni Amavasya that caused 30 deaths, for which " +
                "the government has announced compensation. On a positive note, the Adani group and ISKCON are " +
                "collaborating to run a large-scale religious cloud kitchen, while the Vaishnavite Digambar Ani Akhara " +
                "is modernizing its structure by introducing democratic elections.\"";

        SentimentData sentimentData = getLlamaResponse(prompt);

        if (sentimentData != null) {
            SentimentAnalysis sentimentAnalysis = sentimentData.getSentimentAnalysis();

            if (sentimentAnalysis != null) {
                //System.out.println("Extracted Sentiment Analysis Data: " + sentimentAnalysis);

                Map<String, Object> variables = Map.of(
                        "text", sentimentAnalysis.getText(),
                        "sentiment", sentimentAnalysis.getSentiment(),
                        "score", sentimentAnalysis.getScore(),
                        "category", sentimentAnalysis.getCategory(),
                        "keywords", sentimentAnalysis.getKeywords(),
                        "language", sentimentAnalysis.getLanguage(),
                        "toxicity", sentimentAnalysis.getToxicity(),
                        "subjective", sentimentAnalysis.getSubjectivity(),
                        "timestamp", sentimentAnalysis.getTimestamp()
                );



            } else {
                System.err.println("Sentiment analysis object is null.");
            }
                String text = sentimentAnalysis.getText();
                String sentiment = sentimentAnalysis.getSentiment();
                float score = sentimentAnalysis.getScore();
                String category = sentimentAnalysis.getCategory();
                String[] keywords = sentimentAnalysis.getKeywords();
                String language = sentimentAnalysis.getLanguage();
                float toxicity = sentimentAnalysis.getToxicity();
                float subjective = sentimentAnalysis.getSubjectivity();
                String timestamp = sentimentAnalysis.getTimestamp();

                Map<String, Object> keywordMap = new HashMap<>();
                for (int i = 0; i < keywords.length; i++) {
                    keywordMap.put("keyword_" + (i + 1), keywords[i]);
                }


                Map<String, Object> variables = new HashMap<>();
                variables.put("text", text);
                variables.put("sentiment", sentiment);
                variables.put("score", score);
                variables.put("category", category);
                //Tried all these ways
                //variables.put("keywords", keywordMap);
                //variables.put("keywords", Arrays.toString(keywords));
                //variables.put("keywords", objectMapper.writeValueAsString(keywords));
                //variables.put("keywords", String.join(", ", keywords)); // Convert array to string
                //variables.put("keywords", Arrays.stream(keywords).toArray()); // Convert array to a comma-separated string if needed
                variables.put("language", language);
                variables.put("toxicity", toxicity);
                variables.put("subjective", subjective);
                variables.put("timestamp", timestamp);

                System.out.println("Text: " + text);
                System.out.println("Sentiment: " + sentiment);
                System.out.println("Score: " + score);
                System.out.println("Category: " + category);
                System.out.println("Keywords: " + Arrays.toString(Arrays.stream(keywords).toArray())); // For array, using Arrays.toString() to print the elements
                System.out.println("Language: " + language);
                System.out.println("Toxicity: " + toxicity);
                System.out.println("Subjectivity: " + subjective);
                System.out.println("Timestamp: " + timestamp);

                try {
                    jobClient.newCompleteCommand(job.getKey())//to avoid loop execution
                            .variables(variables)
                            .send()
                            .join();
                    System.out.println("Job completed successfully with sentiment analysis data.");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Failed to complete job in Camunda.");
                }




        }

    }

    private SentimentData getLlamaResponse(String prompt) {
        try {
            Map<String, Object> requestPayload = Map.of(
                    "model", "llama3",
                    "prompt", prompt,
                    "stream", false
            );

            String jsonResponse = restTemplate.postForObject(OLLAMA_API_URL, requestPayload, String.class);
            System.out.println("Raw JSON Response from Llama: " + jsonResponse);

            ObjectMapper initialMapper = new ObjectMapper();
            JsonNode rootNode = initialMapper.readTree(jsonResponse);
            String sentimentJsonString = rootNode.get("response").asText();

            System.out.println("Extracted Sentiment JSON: " + sentimentJsonString);
            sentimentJsonString = sentimentJsonString.substring(sentimentJsonString.indexOf('{'));

            return objectMapper.readValue(sentimentJsonString, SentimentData.class);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error processing request: " + e.getMessage());
            return null;
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class SentimentData {
    @JsonProperty("sentiment_analysis")
    private SentimentAnalysis sentimentAnalysis;

    public SentimentAnalysis getSentimentAnalysis() {
        return sentimentAnalysis;
    }

    public void setSentimentAnalysis(SentimentAnalysis sentimentAnalysis) {
        this.sentimentAnalysis = sentimentAnalysis;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class SentimentAnalysis {
    private String text;
    private String sentiment;
    private float score;
    private String category;
    private String[] keywords;
    private String language;
    private float toxicity;
    private float subjectivity;
    private String timestamp;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }
    public float getScore() { return score; }
    public void setScore(float score) { this.score = score; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String[] getKeywords() { return keywords; }
    public void setKeywords(String[] keywords) { this.keywords = keywords; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public float getToxicity() { return toxicity; }
    public void setToxicity(float toxicity) { this.toxicity = toxicity; }
    public float getSubjectivity() { return subjectivity; }
    public void setSubjectivity(float subjectivity) { this.subjectivity = subjectivity; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
