package com.softpeces.ia;

import java.util.Random;

public class IAServiceStub implements IAService {
    private final Random rnd = new Random();
    @Override
    public Prediction predict(String imagePath, String parte) {
        double p = 0.55 + rnd.nextDouble()*0.35; // 0.55–0.90
        String label = p >= 0.7 ? "Aceptable" : "No aceptable";
        return new Prediction(label, p, "stub-1.0");
    }
}
