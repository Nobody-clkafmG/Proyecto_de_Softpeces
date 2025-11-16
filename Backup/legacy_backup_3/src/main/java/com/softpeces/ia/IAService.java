package com.softpeces.ia;

public interface IAService {
    Prediction predict(String imagePath, String parte) throws Exception;
    record Prediction(String label, double prob, String modelVersion) {}
}
