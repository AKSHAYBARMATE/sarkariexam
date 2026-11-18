package com.helixion.sarkariPariksha.Response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MonthlyScheduleResponse {

    private Integer year;
    private Integer month;

    // Map of Day -> List of categories on that day (e.g. 16 -> ["MOCK_TEST"], 18 -> ["REVISION"])
    private Map<Integer, List<String>> dayDots;

    // Total counts for this month
    private Integer totalMocksThisMonth;
    private Integer totalRevisionsThisMonth;
    private Integer totalExamsThisMonth;
}
