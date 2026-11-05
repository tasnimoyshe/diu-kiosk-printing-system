package com.print.kiosk.service;

import com.print.kiosk.entity.PrintColor;
import com.print.kiosk.entity.PrintFileType;
import com.print.kiosk.entity.PrintingSide;
import com.print.kiosk.service.impl.PricingCalculator;
import com.print.kiosk.service.model.AnalyzedUpload;
import com.print.kiosk.service.model.PricingCalculation;
import com.print.kiosk.service.model.PricingSnapshot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingCalculatorTest {

    private final PricingCalculator calculator =
            new PricingCalculator();

    private final PricingSnapshot prices =
            new PricingSnapshot(
                    1L,
                    4,
                    new BigDecimal("10.00"),
                    new BigDecimal("2.00"),
                    new BigDecimal("18.00"),
                    new BigDecimal("3.50")
            );

    @Test
    void sevenSingleSidedPagesRequireSevenSheets() {
        PricingCalculation result =
                calculate(
                        7,
                        1,
                        PrintColor.BLACK_AND_WHITE,
                        PrintingSide.SINGLE_SIDED
                );

        assertEquals(
                7,
                result.files()
                        .get(0)
                        .physicalSheetsPerCopy()
        );

        assertEquals(
                7,
                result.totalPhysicalSheets()
        );
    }

    @Test
    void sevenDoubleSidedPagesRequireFourSheets() {
        PricingCalculation result =
                calculate(
                        7,
                        1,
                        PrintColor.BLACK_AND_WHITE,
                        PrintingSide.DOUBLE_SIDED
                );

        assertEquals(
                4,
                result.files()
                        .get(0)
                        .physicalSheetsPerCopy()
        );

        assertEquals(
                4,
                result.totalPhysicalSheets()
        );
    }

    @Test
    void copiesMultiplyPhysicalSheets() {
        PricingCalculation result =
                calculate(
                        7,
                        3,
                        PrintColor.BLACK_AND_WHITE,
                        PrintingSide.DOUBLE_SIDED
                );

        assertEquals(
                12,
                result.totalPhysicalSheets()
        );

        assertEquals(
                new BigDecimal("42.00"),
                result.totalAmount()
        );
    }

    @Test
    void multipleDocumentSubtotalsAreSummed() {
        PricingCalculation result =
                calculator.calculate(
                        List.of(
                                upload("one.pdf", 3),
                                upload("two.pdf", 4)
                        ),
                        PrintColor.COLOR,
                        PrintingSide.SINGLE_SIDED,
                        2,
                        prices
                );

        assertEquals(
                7,
                result.totalDocumentPages()
        );

        assertEquals(
                14,
                result.totalPhysicalSheets()
        );

        assertEquals(
                new BigDecimal("140.00"),
                result.totalAmount()
        );
    }

    @Test
    void correctRateIsSelectedForColorDoubleSided() {
        PricingCalculation result =
                calculate(
                        2,
                        1,
                        PrintColor.COLOR,
                        PrintingSide.DOUBLE_SIDED
                );

        assertEquals(
                new BigDecimal("18.00"),
                result.appliedRate()
        );

        assertEquals(
                new BigDecimal("18.00"),
                result.totalAmount()
        );
    }

    @Test
    void decimalMoneyCalculationIsExact() {
        PricingSnapshot decimalPrices =
                new PricingSnapshot(
                        1L,
                        1,
                        new BigDecimal("0.10"),
                        new BigDecimal("0.10"),
                        new BigDecimal("0.10"),
                        new BigDecimal("0.10")
                );

        PricingCalculation result =
                calculator.calculate(
                        List.of(
                                upload("one.pdf", 3)
                        ),
                        PrintColor.COLOR,
                        PrintingSide.SINGLE_SIDED,
                        1,
                        decimalPrices
                );

        assertEquals(
                new BigDecimal("0.30"),
                result.totalAmount()
        );
    }

    private PricingCalculation calculate(
            int pages,
            int copies,
            PrintColor color,
            PrintingSide side
    ) {
        return calculator.calculate(
                List.of(
                        upload("test.pdf", pages)
                ),
                color,
                side,
                copies,
                prices
        );
    }

    private AnalyzedUpload upload(
            String name,
            int pages
    ) {
        return new AnalyzedUpload(
                name,
                PrintFileType.PDF,
                "application/pdf",
                100,
                pages,
                new byte[]{1, 2, 3}
        );
    }
}