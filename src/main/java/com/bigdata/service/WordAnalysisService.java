package com.bigdata.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WordAnalysisService {

    private static final Pattern WORD_PATTERN = Pattern.compile("[\\p{L}]+");
    private static final Pattern STOPWORDS_PATTERN = Pattern.compile(
        "\\b(el|la|de|que|y|a|en|un|es|se|no|te|lo|le|da|su|por|son|con|para|al|del|las|los|una|sobre|todo|pero|más|me|hasta|muy|ha|donde|quien|entre|sin|puede|tanto|cada|fue|uno|dos|tres|cuatro|cinco|seis|siete|ocho|nueve|diez|así|también|cuando|como|si|ya|este|esta|esto|ese|esa|eso|aquí|ahí|allí|ser|estar|tener|hacer|decir|poder|ir|ver|dar|saber|querer|llegar|pasar|deber|poner|venir|salir|volver|seguir|llevar|quedar|traer|desde|contra|durante)\\b",
        Pattern.CASE_INSENSITIVE
    );
    
    public AnalysisResult analyzeFile(String filePath) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Iniciando análisis de archivo: {}", filePath);
            int numProcessors = Runtime.getRuntime().availableProcessors();
            log.info("Utilizando {} procesadores para análisis paralelo", numProcessors);
            Map<String, Integer> wordCounts = processFileInParallel(filePath, numProcessors);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            Map<String, Integer> filteredWords = wordCounts.entrySet().stream()
                .filter(entry -> entry.getKey().length() >= 3)
                .filter(entry -> !STOPWORDS_PATTERN.matcher(entry.getKey()).matches())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            
            List<WordFrequency> topWords = filteredWords.entrySet().stream()
                .map(entry -> new WordFrequency(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> Integer.compare(b.getCount(), a.getCount()))
                .limit(100)
                .collect(Collectors.toList());
            
            long totalTime = System.currentTimeMillis() - startTime;
            
            log.info("Análisis completado en {} ms", totalTime);
            log.info("Total de palabras únicas encontradas: {}", filteredWords.size());
            log.info("Palabra más frecuente: {} ({})", 
                topWords.isEmpty() ? "N/A" : topWords.get(0).getWord(),
                topWords.isEmpty() ? 0 : topWords.get(0).getCount());
            
            return AnalysisResult.builder()
                .totalWords(wordCounts.values().stream().mapToInt(Integer::intValue).sum())
                .uniqueWords(filteredWords.size())
                .topWords(topWords)
                .processingTimeMs(processingTime)
                .totalTimeMs(totalTime)
                .fileSize(getFileSize(filePath))
                .build();
                
        } catch (Exception e) {
            log.error("Error durante el análisis: ", e);
            throw new RuntimeException("Error procesando archivo: " + e.getMessage(), e);
        }
    }
    
    private Map<String, Integer> processFileInParallel(String filePath, int numThreads) throws IOException, InterruptedException, ExecutionException {
        File file = new File(filePath);
        long fileSize = file.length();
        long chunkSize = fileSize / numThreads;
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Map<String, Integer>>> futures = new ArrayList<>();
        
        try {
            for (int i = 0; i < numThreads; i++) {
                long start = i * chunkSize;
                long end = (i == numThreads - 1) ? fileSize : (i + 1) * chunkSize;
                
                futures.add(executor.submit(() -> processFileChunk(filePath, start, end)));
            }
            
            Map<String, Integer> combinedResults = new ConcurrentHashMap<>();
            for (Future<Map<String, Integer>> future : futures) {
                Map<String, Integer> chunkResult = future.get();
                chunkResult.forEach((word, count) -> 
                    combinedResults.merge(word, count, Integer::sum));
            }
            
            return combinedResults;
            
        } finally {
            executor.shutdown();
        }
    }
    
    private Map<String, Integer> processFileChunk(String filePath, long start, long end) {
        Map<String, Integer> wordCounts = new HashMap<>();
        
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            file.seek(start);
            
            if (start > 0) {
                String line = file.readLine();
                start = file.getFilePointer();
            }
            
            StringBuilder buffer = new StringBuilder();
            long currentPos = start;
            
            while (currentPos < end) {
                String line = file.readLine();
                if (line == null) break;
                
                buffer.append(line).append(" ");
                currentPos = file.getFilePointer();
            }
            
            processText(buffer.toString(), wordCounts);
            
        } catch (IOException e) {
            log.error("Error procesando chunk del archivo", e);
        }
        
        return wordCounts;
    }
    
    private void processText(String text, Map<String, Integer> wordCounts) {
        String normalizedText = Normalizer.normalize(text.toLowerCase(), Normalizer.Form.NFD)
            .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");

        java.util.regex.Matcher matcher = WORD_PATTERN.matcher(normalizedText);
        while (matcher.find()) {
            String word = matcher.group();
            if (word.length() >= 3) {
                wordCounts.merge(word, 1, Integer::sum);
            }
        }
    }
    
    private long getFileSize(String filePath) {
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            return 0;
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class AnalysisResult {
        private int totalWords;
        private int uniqueWords;
        private List<WordFrequency> topWords;
        private long processingTimeMs;
        private long totalTimeMs;
        private long fileSize;
        private double wordsPerSecond;
        
        public double getWordsPerSecond() {
            return totalTimeMs > 0 ? (double) totalWords / (totalTimeMs / 1000.0) : 0;
        }
        
        public String getFormattedFileSize() {
            if (fileSize < 1024) return fileSize + " B";
            if (fileSize < 1024 * 1024) return String.format("%.2f KB", fileSize / 1024.0);
            return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
        }
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class WordFrequency {
        private String word;
        private int count;
    }
}
