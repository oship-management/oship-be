package org.example.oshipserver.domain.order.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StateCode {

    // Canada
    AB("AB", "Alberta", CountryCode.CA),
    BC("BC", "British Columbia", CountryCode.CA),
    MB("MB", "Manitoba", CountryCode.CA),
    NB("NB", "New Brunswick", CountryCode.CA),
    NL_CA("NL", "Newfoundland", CountryCode.CA),
    NT("NT", "Northwest Territories", CountryCode.CA),
    NS("NS", "Nova Scotia", CountryCode.CA),
    NU("NU", "Nunavut", CountryCode.CA),
    ON("ON", "Ontario", CountryCode.CA),
    PE("PE", "Prince Edward Island", CountryCode.CA),
    QC("QC", "Quebec", CountryCode.CA),
    SK("SK", "Saskatchewan", CountryCode.CA),
    YT("YT", "Yukon", CountryCode.CA),

    // India
    AN("AN", "Andaman & Nicobar (U.T)", CountryCode.IN),
    AP("AP", "Andhra Pradesh", CountryCode.IN),
    AR("AR", "Arunachal Pradesh", CountryCode.IN),
    AS("AS", "Assam", CountryCode.IN),
    BR("BR", "Bihar", CountryCode.IN),
    CG("CG", "Chattisgarh", CountryCode.IN),
    CH("CH", "Chandigarh (U.T.)", CountryCode.IN),
    DD("DD", "Daman & Diu (U.T.)", CountryCode.IN),
    DL("DL", "Delhi (U.T.)", CountryCode.IN),
    DN("DN", "Dadra and Nagar Haveli (U.T.)", CountryCode.IN),
    GA_IN("GA", "Goa", CountryCode.IN),
    GJ("GJ", "Gujarat", CountryCode.IN),
    HR("HR", "Haryana", CountryCode.IN),
    HP("HP", "Himachal Pradesh", CountryCode.IN),
    JK("JK", "Jammu & Kashmir", CountryCode.IN),
    JH("JH", "Jharkhand", CountryCode.IN),
    KA("KA", "Karnataka", CountryCode.IN),
    KL("KL", "Kerala", CountryCode.IN),
    LD("LD", "Lakshadweep (U.T)", CountryCode.IN),
    MP("MP", "Madhya Pradesh", CountryCode.IN),
    MH_IN("MH", "Maharashtra", CountryCode.IN),
    MN("MN", "Manipur", CountryCode.IN),
    ML("ML", "Meghalaya", CountryCode.IN),
    MZ("MZ", "Mizoram", CountryCode.IN),
    NL_IN("NL", "Nagaland", CountryCode.IN),
    OR("OR", "Orissa", CountryCode.IN),
    PB("PB", "Punjab", CountryCode.IN),
    PY("PY", "Puducherry (U.T.)", CountryCode.IN),
    RJ("RJ", "Rajasthan", CountryCode.IN),
    SK_IN("SK", "Sikkim", CountryCode.IN),
    TN("TN", "Tamil Nadu", CountryCode.IN),
    TR("TR", "Tripura", CountryCode.IN),
    UA("UA", "Uttaranchal", CountryCode.IN),
    UP("UP", "Uttar Pradesh", CountryCode.IN),
    WB("WB", "West Bengal", CountryCode.IN),

    // Mexico
    AG("AG", "Aguascalientes", CountryCode.MX),
    BC_MX("BC", "Baja California", CountryCode.MX),
    BS("BS", "Baja California Sur", CountryCode.MX),
    CM("CM", "Campeche", CountryCode.MX),
    CS("CS", "Chiapas", CountryCode.MX),
    CH_MX("CH", "Chihuahua", CountryCode.MX),
    DF("DF", "Ciudad de México", CountryCode.MX),
    CO("CO", "Coahuila", CountryCode.MX),
    CL("CL", "Colima", CountryCode.MX),
    DG("DG", "Durango", CountryCode.MX),
    EM("EM", "Estado de México", CountryCode.MX),
    GT("GT", "Guanajuato", CountryCode.MX),
    GR("GR", "Guerrero", CountryCode.MX),
    HG("HG", "Hidalgo", CountryCode.MX),
    JA("JA", "Jalisco", CountryCode.MX),
    MI_MX("MI", "Michoacán", CountryCode.MX),
    MO_MX("MO", "Morelos", CountryCode.MX),
    NA("NA", "Nayarit", CountryCode.MX),
    NL_MX("NL", "Nuevo León", CountryCode.MX),
    OA("OA", "Oaxaca", CountryCode.MX),
    PU("PU", "Puebla", CountryCode.MX),
    QE("QE", "Querétaro", CountryCode.MX),
    QR("QR", "Quintana Roo", CountryCode.MX),
    SL("SL", "San Luis Potosí", CountryCode.MX),
    SI("SI", "Sinaloa", CountryCode.MX),
    SO_MX("SO", "Sonora", CountryCode.MX),
    TB("TB", "Tabasco", CountryCode.MX),
    TM("TM", "Tamaulipas", CountryCode.MX),
    TL("TL", "Tlaxcala", CountryCode.MX),
    VE("VE", "Veracruz", CountryCode.MX),
    YU("YU", "Yucatán", CountryCode.MX),
    ZA("ZA", "Zacatecas", CountryCode.MX),

    // United States
    AL("AL", "Alabama", CountryCode.US),
    AK("AK", "Alaska", CountryCode.US),
    AZ("AZ", "Arizona", CountryCode.US),
    AR_US("AR", "Arkansas", CountryCode.US),
    CA_US("CA", "California", CountryCode.US),
    CO_US("CO", "Colorado", CountryCode.US),
    CT("CT", "Connecticut", CountryCode.US),
    DE("DE", "Delaware", CountryCode.US),
    DC("DC", "District of Columbia", CountryCode.US),
    FL("FL", "Florida", CountryCode.US),
    GA_US("GA", "Georgia", CountryCode.US),
    HI("HI", "Hawaii", CountryCode.US),
    ID("ID", "Idaho", CountryCode.US),
    IL("IL", "Illinois", CountryCode.US),
    IN_US("IN", "Indiana", CountryCode.US),
    IA("IA", "Iowa", CountryCode.US),
    KS("KS", "Kansas", CountryCode.US),
    KY("KY", "Kentucky", CountryCode.US),
    LA("LA", "Louisiana", CountryCode.US),
    ME("ME", "Maine", CountryCode.US),
    MD("MD", "Maryland", CountryCode.US),
    MA("MA", "Massachusetts", CountryCode.US),
    MI_US("MI", "Michigan", CountryCode.US),
    MN_US("MN", "Minnesota", CountryCode.US),
    MS("MS", "Mississippi", CountryCode.US),
    MO_US("MO", "Missouri", CountryCode.US),
    MT("MT", "Montana", CountryCode.US),
    NE("NE", "Nebraska", CountryCode.US),
    NV("NV", "Nevada", CountryCode.US),
    NH("NH", "New Hampshire", CountryCode.US),
    NJ("NJ", "New Jersey", CountryCode.US),
    NM("NM", "New Mexico", CountryCode.US),
    NY("NY", "New York", CountryCode.US),
    NC("NC", "North Carolina", CountryCode.US),
    ND("ND", "North Dakota", CountryCode.US),
    OH("OH", "Ohio", CountryCode.US),
    OK("OK", "Oklahoma", CountryCode.US),
    OR_US("OR", "Oregon", CountryCode.US),
    PA("PA", "Pennsylvania", CountryCode.US),
    RI("RI", "Rhode Island", CountryCode.US),
    SC("SC", "South Carolina", CountryCode.US),
    SD("SD", "South Dakota", CountryCode.US),
    TN_US("TN", "Tennessee", CountryCode.US),
    TX("TX", "Texas", CountryCode.US),
    UT("UT", "Utah", CountryCode.US),
    VT("VT", "Vermont", CountryCode.US),
    VA("VA", "Virginia", CountryCode.US),
    WA("WA", "Washington State", CountryCode.US),
    WV("WV", "West Virginia", CountryCode.US),
    WI("WI", "Wisconsin", CountryCode.US),
    WY("WY", "Wyoming", CountryCode.US),
    PR_US("PR", "Puerto Rico", CountryCode.US),

    // UAE
    AB_AE("AB", "Abu Dhabi", CountryCode.AE),
    AJ("AJ", "Ajman", CountryCode.AE),
    DU("DU", "Dubai", CountryCode.AE),
    FU("FU", "Fujairah", CountryCode.AE),
    RA("RA", "Ras al-Khaimah", CountryCode.AE),
    SH_AE("SH", "Sharjah", CountryCode.AE),
    UM("UM", "Umm al-Quwain", CountryCode.AE);

    private final String code; // 실제 주 코드 (중복 허용)
    private final String stateName;
    private final CountryCode countryCode;

    /**
     * 주어진 국가 코드와 주 코드로 StateCode enum 찾음.
     * 만약 code가 null이거나 빈 문자열이면 Optional.empty()를 반환하여
     * 유효하지 않은 값으로 처리하지 않고 무시되도록 함.
     */
    public static Optional<StateCode> from(CountryCode countryCode, String code) {
        // 빈 문자열이나 null이면 매핑하지 않음 (DB에서는 null로 저장됨)
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }

        return Arrays.stream(StateCode.values())
            .filter(state -> state.getCountryCode() == countryCode
                && state.getCode().equalsIgnoreCase(code))
            .findFirst();
    }


    /**
     * JSON 역직렬화 시 빈 문자열("") 혹은 null을 안전하게 처리하기 위한 커스텀 파서
     * ex) JSON에서 "CA" 같은 문자열이 들어오면 해당 code와 일치하는 StateCode enum으로 매핑
     *     빈 문자열이나 null이 들어오면 null 반환 → 에러 방지
     */
    @JsonCreator
    public static StateCode fromJson(String value) {
        if (value == null || value.isBlank()) return null; // 빈 문자열 또는 null이면 null 반환

        for (StateCode stateCode : values()) {
            // code 값이 일치하는 enum을 찾는다 (대소문자 구분 없이)
            if (stateCode.getCode().equalsIgnoreCase(value)) {
                return stateCode;
            }
        }
        return null; // 해당하는 enum이 없으면 null 반환
    }

    /**
     * JSON 직렬화 시 enum 전체 이름이 아니라 code 값만 응답에 사용되도록 지정
     * ex) StateCode.CA_US → "CA"
     */
    @JsonValue
    public String toJson() {
        return this.code;
    }


}
