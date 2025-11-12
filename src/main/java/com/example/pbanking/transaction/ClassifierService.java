package com.example.pbanking.transaction;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.example.pbanking.transaction.dto.response.TransactionsResponse;
import com.example.pbanking.exception.ModelException;
import com.example.pbanking.common.enums.PurposeType;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ClassifierService {
    private static final Logger log = LoggerFactory.getLogger(ClassifierService.class);
    private final OrtEnvironment env;
    private final OrtSession session;

    public ClassifierService(@Value("classpath:models/model.onnx") Resource modelResource) {
        try {
            env = OrtEnvironment.getEnvironment();
            session = env.createSession(extractModel(modelResource), new OrtSession.SessionOptions());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ONNX session", e);
        }
    }
    
    public PurposeType predictType(TransactionsResponse.Transaction transaction) {
        if (transaction == null) {
            return PurposeType.NONE;
        }

        String description = transaction.getTransactionInformation() != null
            ? transaction.getTransactionInformation()
            : "";
        String sanitizedDescription = sanitizeDescription(description);
        float amountValue = (transaction.getAmount() != null && transaction.getAmount().getAmount() != null)
            ? transaction.getAmount().getAmount().floatValue()
            : 0f;

        try (OnnxTensor informationTensor = OnnxTensor.createTensor(env, new String[][]{{sanitizedDescription}});
             OnnxTensor amountTensor = OnnxTensor.createTensor(env, new float[][]{{amountValue}})) {
            Map<String, OnnxTensor> inputs = Map.of(
                "transactionInformation", informationTensor,
                "amount", amountTensor);

            try (OrtSession.Result result = session.run(inputs)) {
                if (result == null || result.size() == 0) {
                    log.warn("Model returned no outputs for transaction {}", transaction.getTransactionId());
                    return PurposeType.NONE;
                }

                OnnxTensor outputTensor = (OnnxTensor) result.get(0);
                String[] labels = extractLabels(outputTensor.getValue());
                PurposeType predicted = determinePurposeType(labels);
                log.info("Classifier labels {} -> {}", Arrays.toString(labels), predicted);
                return predicted;
            }
        } catch (Exception e) {
            throw new ModelException("Failed to predict transaction type", e);
        }
    }

    private String extractModel(Resource modelResource) throws IOException {
        Path tempFile = Files.createTempFile("onnx-model-", ".onnx");
        tempFile.toFile().deleteOnExit();
        try (InputStream inputStream = modelResource.getInputStream()) {
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile.toString();
    }

    private PurposeType determinePurposeType(String[] labels) {
        if (labels == null || labels.length == 0) {
            return PurposeType.NONE;
        }

        for (String label : labels) {
            if (label == null) {
                continue;
            }
            String normalized = label.toLowerCase(Locale.ROOT);
            if (normalized.contains("business")) {
                return PurposeType.BUSINESS;
            }
            if (normalized.contains("life")) {
                return PurposeType.PERSONAL;
            }
        }
        return PurposeType.NONE;
    }

    private String[] extractLabels(Object rawValue) {
        if (rawValue instanceof String[] labels) {
            return labels;
        }
        if (rawValue instanceof String[][] labels2d && labels2d.length > 0) {
            return labels2d[0];
        }
        if (rawValue != null) {
            log.warn("Expected String[] labels but received {}", rawValue.getClass());
        }
        return new String[0];
    }

    private String sanitizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return "";
        }
        String normalized = Normalizer.normalize(description, Normalizer.Form.NFKC);
        StringBuilder builder = new StringBuilder(normalized.length());
        normalized.codePoints().forEach(cp -> {
            if (Character.isLetterOrDigit(cp)
                || Character.isWhitespace(cp)
                || cp == '.' || cp == ',' || cp == '-' || cp == '_' || cp == '/' || cp == '(' || cp == ')') {
                builder.appendCodePoint(cp);
            }
        });
        String sanitized = builder.toString().trim();
        if (sanitized.isEmpty()) {
            log.debug("Sanitized description became empty for input '{}'", description);
            return "";
        }
        return sanitized;
    }
}
