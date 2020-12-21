package io.glnt.gpms.model.enums

enum class checkUseStatus {
    Y, N
}

enum class GateTypeStatus {
    IN, OUT, IN_OUT
}

enum class LprTypeStatus {
    FRONT, BACK, ASSIST
}

enum class DisplayPosition(val code: String, val desc: String) {
    IN("IN", "입구"),
    OUT("OUT", "출구")
}

enum class DisplayType(val code: String, val desc: String) {
    NORMAL1("NORMAL1", "일반(첫번째줄)"),
    NORMAL2("NORMAL2", "일반(두번째줄)"),
    EMPHASIS("EMPHASIS", "강조"),
}

enum class DisplayMessageClass(val code: String, val desc: String) {
    IN("IN", "입차"),
    OUT("OUT", "출차"),
    WAIT("WAIT", "정산대기"),
}

enum class DisplayMessageType(val code: String, val desc: String) {
    MEMBER("MEMBER", "Tmap(회원)차량"),
    NONMEMBER("NONMEMBER", "일반차량"),
    VIP("VIP", "정기권차량"),
    CALL("CALL", "호출"),
    FAIL("FAIL", "실패"),
    FAILNUMBER("FAILNUMBER", "미인식차량")
}

enum class DisplayMessageCode(val code: String, val desc: String) {
    DESC("DESC", "메세지내용"),
    PAYMENT("PAYMENT", "결제금액"),
    CARNUM("CARNUM", "차량번호"),
    REMAINDAYS("REMAINDAYS", "정기권남은일자"),
    FAIL("FAIL", "실패"),
}

enum class SetupOption {
    ADD, UPDATE, DELETE, OVERWRITE
}

enum class TicketType(val code: String, val desc: String) {
    SEASONTICKET("SEASONTICKET", "정기권"),
    WHITELIST("WHITELIST", "화이트리스트"),
    VISITTICKET("VISITTICKET", "방문권"),
    TIMETICKET("TIMETICKET", "시간권"),
    DAYTICKET("DAYTICKET", "일일권"),
    ETC("ETC", "기타"),
}

//enum class parkCarType {
//    "일반차량", "정기권차량", "미인식차량"
//}