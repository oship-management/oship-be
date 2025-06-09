package org.example.oshipserver.domain.payment.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PaymentNoGenerator {
    // 결제 생성시 우리 서버에서 생성해야하는 코드 (PAY-20240604-0001 형식)
    // 결제 PK 및 Toss의 멱등성 KEY로 사용
    public static String generate(LocalDate date, int sequence) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seqStr = String.format("%04d", sequence); // 4자리 패딩
        return "PAY-" + dateStr + "-" + seqStr;
    }
}