package com.fooddelivery.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.fooddelivery.algorithms.result.PriorityScoreResult;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.RequestStatus;

class PriorityScoreCalculatorTest {
    private static final double EPSILON = 0.000000000001;

    @Test
    void approvedRawConstantsAreExposedWithoutAlternativeTotals() {
        assertEquals(58, IndexDerivedPriorityParameters.URGENCY_RAW_WEIGHT);
        assertEquals(74, IndexDerivedPriorityParameters.DEADLINE_RAW_WEIGHT);
        assertEquals(72, IndexDerivedPriorityParameters.WAITING_TIME_RAW_WEIGHT);
        assertEquals(204, IndexDerivedPriorityParameters.TOTAL_RAW_WEIGHT);
    }

    @Test
    void normalizedWeightsUseApprovedFormulasAndSumToOne() {
        assertEquals(58.0 / 204.0, IndexDerivedPriorityParameters.URGENCY_WEIGHT, EPSILON);
        assertEquals(74.0 / 204.0, IndexDerivedPriorityParameters.DEADLINE_WEIGHT, EPSILON);
        assertEquals(72.0 / 204.0, IndexDerivedPriorityParameters.WAITING_TIME_WEIGHT, EPSILON);
        assertEquals(1.0, IndexDerivedPriorityParameters.normalizedWeightTotal(), EPSILON);
        assertEquals(true, IndexDerivedPriorityParameters.weightsSumToOne());
    }

    @Test
    void allWeightsArePositive() {
        assertEquals(true, IndexDerivedPriorityParameters.URGENCY_WEIGHT > 0.0);
        assertEquals(true, IndexDerivedPriorityParameters.DEADLINE_WEIGHT > 0.0);
        assertEquals(true, IndexDerivedPriorityParameters.WAITING_TIME_WEIGHT > 0.0);
    }

    @Test
    void allZeroAndAllOneComponentsStayNormalized() {
        PriorityScoreCalculator calculator = new PriorityScoreCalculator();

        PriorityScoreResult zero = calculator.calculate(0.0, 0.0, 0.0);
        PriorityScoreResult one = calculator.calculate(1.0, 1.0, 1.0);

        assertEquals(0.0, zero.getPriorityScore(), EPSILON);
        assertEquals(1.0, one.getPriorityScore(), EPSILON);
    }

    @Test
    void singleComponentScoresMatchTheirWeights() {
        PriorityScoreCalculator calculator = new PriorityScoreCalculator();

        assertEquals(IndexDerivedPriorityParameters.URGENCY_WEIGHT,
                calculator.calculate(1.0, 0.0, 0.0).getPriorityScore(), EPSILON);
        assertEquals(IndexDerivedPriorityParameters.DEADLINE_WEIGHT,
                calculator.calculate(0.0, 1.0, 0.0).getPriorityScore(), EPSILON);
        assertEquals(IndexDerivedPriorityParameters.WAITING_TIME_WEIGHT,
                calculator.calculate(0.0, 0.0, 1.0).getPriorityScore(), EPSILON);
    }

    @Test
    void mixedValuesReturnDeterministicContributionEvidence() {
        PriorityScoreResult result = new PriorityScoreCalculator().calculate(0.25, 0.50, 0.75);

        assertEquals(0.25, result.getUrgencyComponent(), EPSILON);
        assertEquals(0.50, result.getDeadlinePressureComponent(), EPSILON);
        assertEquals(0.75, result.getWaitingTimeComponent(), EPSILON);
        assertEquals(IndexDerivedPriorityParameters.URGENCY_WEIGHT, result.getUrgencyWeight(), EPSILON);
        assertEquals(IndexDerivedPriorityParameters.DEADLINE_WEIGHT, result.getDeadlineWeight(), EPSILON);
        assertEquals(IndexDerivedPriorityParameters.WAITING_TIME_WEIGHT, result.getWaitingTimeWeight(), EPSILON);
        assertEquals((58.0 / 204.0) * 0.25, result.getUrgencyContribution(), EPSILON);
        assertEquals((74.0 / 204.0) * 0.50, result.getDeadlineContribution(), EPSILON);
        assertEquals((72.0 / 204.0) * 0.75, result.getWaitingTimeContribution(), EPSILON);
        assertEquals(result.getUrgencyContribution()
                + result.getDeadlineContribution()
                + result.getWaitingTimeContribution(), result.getPriorityScore(), EPSILON);
    }

    @Test
    void rejectsInvalidComponentsWithoutClamping() {
        PriorityScoreCalculator calculator = new PriorityScoreCalculator();

        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(-0.01, 0.0, 0.0));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(0.0, -0.01, 0.0));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(0.0, 0.0, -0.01));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(1.01, 0.0, 0.0));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(0.0, 1.01, 0.0));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(0.0, 0.0, 1.01));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(Double.NaN, 0.0, 0.0));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(0.0, Double.POSITIVE_INFINITY, 0.0));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(0.0, 0.0, Double.NEGATIVE_INFINITY));
    }

    @Test
    void pureCalculationDoesNotMutateDeliveryRequest() {
        DeliveryRequest request = request();
        double originalPriority = request.getPriorityScore();
        RequestStatus originalStatus = request.getStatus();

        new PriorityScoreCalculator().calculate(1.0, 1.0, 1.0);

        assertEquals(originalPriority, request.getPriorityScore(), EPSILON);
        assertEquals(originalStatus, request.getStatus());
    }

    @Test
    void productionSourceDoesNotExposeStudentIdentifiersOrInventedRanges() throws IOException {
        String parameters = Files.readString(Path.of(
                "src/main/java/com/fooddelivery/algorithms/IndexDerivedPriorityParameters.java"));
        String calculator = Files.readString(Path.of(
                "src/main/java/com/fooddelivery/algorithms/PriorityScoreCalculator.java"));
        String production = parameters + calculator;

        assertFalse(production.matches("(?s).*[1-9][0-9]{7,}.*"));
        assertFalse(production.contains("studentName"));
        assertFalse(production.contains("indexNumber"));
        assertFalse(production.contains("1-5"));
        assertFalse(production.contains("1–5"));
        assertFalse(production.contains("System.out"));
        assertFalse(production.contains("java.util."));
    }

    private static DeliveryRequest request() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 8, 0);
        return new DeliveryRequest("REQ-P", "LOC-A", "LOC-B", "Food", 1, 1.0,
                now, now.plusHours(1), RequestStatus.PENDING, 0.25);
    }
}
