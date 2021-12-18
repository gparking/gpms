package io.glnt.gpms.service

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.utils.*
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.dashboard.admin.model.ReqCreateMessage
import io.glnt.gpms.handler.facility.model.*
import io.glnt.gpms.handler.inout.model.reqUpdatePayment
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.tmap.model.*
import io.glnt.gpms.handler.tmap.service.TmapSendService
import io.glnt.gpms.model.dto.entity.FacilityDTO
import io.glnt.gpms.model.dto.entity.GateDTO
import io.glnt.gpms.model.dto.request.reqDisplayInfo
import io.glnt.gpms.model.entity.*
import io.glnt.gpms.model.enums.*
import io.glnt.gpms.model.mapper.FacilityMapper
import io.glnt.gpms.model.repository.*
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.annotation.PostConstruct

@Service
class FacilityService(
    private var displayInfoRepository: DisplayInfoRepository,
    private var displayColorRepository: DisplayColorRepository,
    private var displayMessageRepository: DisplayMessageRepository,
    private var facilityMapper: FacilityMapper,
    private var gateService: GateService,
    private var parkSiteInfoService: ParkSiteInfoService
) {
    companion object : KLogging()

    lateinit var gates: List<GateDTO>

    /* static */
    lateinit var displayColors: List<DisplayColor>

//    @Value("\${gateway.url}")
//    lateinit var url: String

    @Value("\${tmap.send}")
    lateinit var tmapSend: String

//    @Autowired
//    private lateinit var displayColorRepository: DisplayColorRepository
//
//    @Autowired
//    private lateinit var displayMessageRepository: DisplayMessageRepository

    @Autowired
    private lateinit var tmapSendService: TmapSendService

    @Autowired
    private lateinit var inoutService: InoutService

    @Autowired
    private lateinit var parkinglotService: ParkinglotService

    @Autowired
    private lateinit var relayService: RelayService

    @Autowired
    private lateinit var restAPIManager: RestAPIManagerUtil

    @Autowired
    private lateinit var parkGateRepository: ParkGateRepository

    @Autowired
    private lateinit var facilityRepository: ParkFacilityRepository

    @Autowired
    private lateinit var failureRepository: FailureRepository

    @PostConstruct
    fun initalizeData() {
        gateService.findAll().let {
            gates = it
        }

        facilityRepository.findByDtFacilitiesId("LPR001101")?: run {
            facilityRepository.saveAndFlush(
                Facility(sn = null, category = FacilityCategoryType.LPR, modelid = "MDL0000029", fname = "입구1 LPR", dtFacilitiesId = "LPR001101", gateId = "GATE001",
                    ip = "192.168.20.101", port = "0", resetPort = 1, gateType = GateTypeStatus.IN, imagePath = "C:\\park\\in_front", lprType = LprTypeStatus.FRONT, delYn = DelYn.N))
        }
        facilityRepository.findByDtFacilitiesId("LPR001201")?: run {
            facilityRepository.saveAndFlush(
                Facility(sn = null, category = FacilityCategoryType.LPR, modelid = "MDL0000029", fname = "입구1 LPR(후방)", dtFacilitiesId = "LPR001201", gateId = "GATE001",
                    ip = "192.168.20.102", port = "0", resetPort = 1, gateType = GateTypeStatus.IN, imagePath = "C:\\park\\in_back", lprType = LprTypeStatus.BACK, delYn = DelYn.N))
        }
        facilityRepository.findByDtFacilitiesId("DSP001101")?: run {
            facilityRepository.saveAndFlush(
                Facility(sn = null, category = FacilityCategoryType.DISPLAY, modelid = "MDL0000043", fname = "입구1 전광판", dtFacilitiesId = "DSP001101", gateId = "GATE001",
                    ip = "192.168.20.111", port = "5000", resetPort = 1, gateType = GateTypeStatus.IN, delYn = DelYn.N))
        }
        facilityRepository.findByDtFacilitiesId("BRE001101")?: run {
            facilityRepository.saveAndFlush(
                Facility(sn = null, category = FacilityCategoryType.BREAKER, modelid = "MDL0000035", fname = "입구1 차단기", dtFacilitiesId = "BRE001101", gateId = "GATE001",
                    ip = "192.168.20.121", port = "4001", resetPort = 2, gateType = GateTypeStatus.IN, delYn = DelYn.N))
        }
        facilityRepository.findByDtFacilitiesId("LPR001102")?: run {
            facilityRepository.saveAndFlush(
                Facility(sn = null, category = FacilityCategoryType.LPR, modelid = "MDL0000029", fname = "입구1 보조 LPR", dtFacilitiesId = "LPR001102", gateId = "GATE001",
                    ip = "0.0.0.0", port = "0", resetPort = -1, gateType = GateTypeStatus.IN, lprType = LprTypeStatus.ASSIST, delYn = DelYn.N))
        }
        facilityRepository.findByDtFacilitiesId("LPR002101")?: run {
            facilityRepository.saveAndFlush(
                Facility(sn = null, category = FacilityCategoryType.LPR, modelid = "MDL0000029", fname = "출구1 LPR", dtFacilitiesId = "LPR002101", gateId = "GATE002",
                    ip = "192.168.20.103", port = "0", resetPort = 3, gateType = GateTypeStatus.OUT, imagePath = "C:\\park\\out_front", lprType = LprTypeStatus.FRONT, delYn = DelYn.N))
        }
        facilityRepository.findByDtFacilitiesId("DSP002201")?: run {
            facilityRepository.saveAndFlush(
                Facility(sn = null, category = FacilityCategoryType.DISPLAY, modelid = "MDL0000043", fname = "출구1 전광판", dtFacilitiesId = "DSP002201", gateId = "GATE002",
                    ip = "192.168.20.112", port = "5000", resetPort = 3, gateType = GateTypeStatus.OUT, delYn = DelYn.N))
        }
        facilityRepository.findByDtFacilitiesId("BRE002201")?: run {
            facilityRepository.saveAndFlush(
                Facility(sn = null, category = FacilityCategoryType.BREAKER, modelid = "MDL0000035", fname = "출구1 차단기", dtFacilitiesId = "BRE002201", gateId = "GATE002",
                    ip = "192.168.20.122", port = "4001", resetPort = 4, gateType = GateTypeStatus.OUT, delYn = DelYn.N))
        }
        facilityRepository.findByDtFacilitiesId("LPR002102")?: run {
            facilityRepository.saveAndFlush(
                Facility(sn = null, category = FacilityCategoryType.LPR, modelid = "MDL0000029", fname = "출구1 보조 LPR", dtFacilitiesId = "LPR002102", gateId = "GATE002",
                    ip = "0.0.0.0", port = "0", resetPort = -1, gateType = GateTypeStatus.OUT, lprType = LprTypeStatus.ASSIST, delYn = DelYn.N))
        }
        facilityRepository.findByDtFacilitiesId("PAY002201")?: run {
            facilityRepository.saveAndFlush(
                Facility(sn = null, category = FacilityCategoryType.PAYSTATION, modelid = "MDL0000030", fname = "출구1 정산기", dtFacilitiesId = "PAY002201", gateId = "GATE002",
                    ip = "192.168.20.131", port = "7373", resetPort = 5, gateType = GateTypeStatus.OUT, delYn = DelYn.N))
        }
        facilityRepository.findByDtFacilitiesId("VOP002201")?: run {
            facilityRepository.saveAndFlush(
                Facility(sn = null, category = FacilityCategoryType.VOIP, modelid = "MDL0000032", fname = "출구1 VOIP", dtFacilitiesId = "VOP002201", gateId = "GATE002",
                    ip = "192.168.20.142", port = "0", resetPort = -1, gateType = GateTypeStatus.OUT, delYn = DelYn.N))
        }
        facilityRepository.findByDtFacilitiesId("LPR003101")?: run {
            facilityRepository.saveAndFlush(
                Facility(sn = null, category = FacilityCategoryType.LPR, modelid = "MDL0000029", fname = "입구2 LPR", dtFacilitiesId = "LPR003101", gateId = "GATE003",
                    ip = "192.168.20.104", port = "0", resetPort = -1, gateType = GateTypeStatus.IN, imagePath = "C:\\park\\in_front2", lprType = LprTypeStatus.FRONT, delYn = DelYn.N))
        }
        facilityRepository.findByDtFacilitiesId("LPR003201")?: run {
            facilityRepository.saveAndFlush(
                Facility(sn = null, category = FacilityCategoryType.LPR, modelid = "MDL0000029", fname = "입구2 LPR(후방)", dtFacilitiesId = "LPR003201", gateId = "GATE003",
                    ip = "192.168.20.105", port = "0", resetPort = -1, gateType = GateTypeStatus.IN, imagePath = "C:\\park\\in_back2", lprType = LprTypeStatus.BACK, delYn = DelYn.N))
        }
        facilityRepository.findByDtFacilitiesId("DSP003101")?: run {
            facilityRepository.saveAndFlush(
                Facility(sn = null, category = FacilityCategoryType.DISPLAY, modelid = "MDL0000043", fname = "입구2 전광판", dtFacilitiesId = "DSP003101", gateId = "GATE003",
                    ip = "192.168.20.113", port = "5000", resetPort = 0, gateType = GateTypeStatus.IN, delYn = DelYn.N))
        }
        facilityRepository.findByDtFacilitiesId("BRE003101")?: run {
            facilityRepository.saveAndFlush(
                Facility(sn = null, category = FacilityCategoryType.BREAKER, modelid = "MDL0000035", fname = "입구2 차단기", dtFacilitiesId = "BRE003101", gateId = "GATE003",
                    ip = "192.168.20.123", port = "4001", resetPort = 0, gateType = GateTypeStatus.IN, delYn = DelYn.N))
        }
        facilityRepository.findByDtFacilitiesId("LPR003102")?: run {
            facilityRepository.saveAndFlush(
                Facility(sn = null, category = FacilityCategoryType.LPR, modelid = "MDL0000029", fname = "입구2 보조 LPR", dtFacilitiesId = "LPR003102", gateId = "GATE003",
                    ip = "0.0.0.0", port = "0", resetPort = 0, gateType = GateTypeStatus.IN, lprType = LprTypeStatus.ASSIST, delYn = DelYn.N))
        }
        facilityRepository.findByDtFacilitiesId("LPR004101")?: run {
            facilityRepository.saveAndFlush(
                Facility(sn = null, category = FacilityCategoryType.LPR, modelid = "MDL0000029", fname = "출구2 LPR", dtFacilitiesId = "LPR004101", gateId = "GATE004",
                    ip = "192.168.20.106", port = "0", resetPort = 0, gateType = GateTypeStatus.OUT, imagePath = "C:\\park\\out_front2", lprType = LprTypeStatus.FRONT, delYn = DelYn.N))
        }
        facilityRepository.findByDtFacilitiesId("DSP004201")?: run {
            facilityRepository.saveAndFlush(
                Facility(sn = null, category = FacilityCategoryType.DISPLAY, modelid = "MDL0000043", fname = "출구2 전광판", dtFacilitiesId = "DSP004201", gateId = "GATE004",
                    ip = "192.168.20.114", port = "5000", resetPort = 0, gateType = GateTypeStatus.OUT, delYn = DelYn.N))
        }
        facilityRepository.findByDtFacilitiesId("BRE004201")?: run {
            facilityRepository.saveAndFlush(
                Facility(sn = null, category = FacilityCategoryType.BREAKER, modelid = "MDL0000035", fname = "출구2 차단기", dtFacilitiesId = "BRE004201", gateId = "GATE004",
                    ip = "192.168.20.124", port = "4001", resetPort = 0, gateType = GateTypeStatus.OUT, delYn = DelYn.N))
        }
        facilityRepository.findByDtFacilitiesId("LPR004102")?: run {
            facilityRepository.saveAndFlush(
                Facility(sn = null, category = FacilityCategoryType.LPR, modelid = "MDL0000029", fname = "출구2 보조 LPR", dtFacilitiesId = "LPR004102", gateId = "GATE004",
                    ip = "0.0.0.0", port = "0", resetPort = 0, gateType = GateTypeStatus.OUT, lprType = LprTypeStatus.ASSIST, delYn = DelYn.N))
        }
        facilityRepository.findByDtFacilitiesId("PAY004201")?: run {
            facilityRepository.saveAndFlush(
                Facility(sn = null, category = FacilityCategoryType.PAYSTATION, modelid = "MDL0000030", fname = "출구2 정산기", dtFacilitiesId = "PAY004201", gateId = "GATE004",
                    ip = "192.168.20.132", port = "7373", resetPort = 0, gateType = GateTypeStatus.OUT, delYn = DelYn.N))
        }
        facilityRepository.findByDtFacilitiesId("VOP004201")?: run {
            facilityRepository.saveAndFlush(
                Facility(sn = null, category = FacilityCategoryType.VOIP, modelid = "MDL0000032", fname = "출구2 VOIP", dtFacilitiesId = "VOP004201", gateId = "GATE004",
                    ip = "192.168.20.144", port = "0", resetPort = 0, gateType = GateTypeStatus.OUT, delYn = DelYn.N))
        }

    }

    fun setDisplayColor(request: ArrayList<reqSetDisplayColor>): CommonResult = with(request) {
        logger.info { "setDisplayColor request $request" }
        try {
            request.forEach { it ->
                displayColorRepository.findByColorCode(it.colorCode)?.let { displayColor ->
                    displayColor.colorCode = it.colorCode
                    displayColor.colorDesc = it.colorDesc
                    displayColorRepository.save(displayColor)
                } ?: run {
                    displayColorRepository.save(
                        DisplayColor(
                            sn = null, colorCode = it.colorCode, colorDesc = it.colorDesc
                        )
                    )
                }
            }
            return CommonResult.created("parkinglot display setting success")

        } catch (e: RuntimeException) {
            logger.error("set display color error {} ", e.message)
            return CommonResult.error("parkinglot display setting failed ")
        }
    }

    fun setDisplayMessage(request: ArrayList<reqSetDisplayMessage>): CommonResult = with(request) {
        logger.info { "setDisplayMessage request $request" }
        try {
            val data = ArrayList<DisplayMessage>()
            request.forEach { it ->
                displayMessageRepository.findByMessageClassAndMessageTypeAndOrder(
                    it.messageClass!!,
                    it.messageType!!,
                    it.order
                )?.let { displayMessage ->
                    displayMessage.colorCode = it.colorCode
                    displayMessage.messageDesc = it.messageDesc
                    displayMessage.lineNumber = it.line
                    data.add(displayMessageRepository.save(displayMessage));
                } ?: run {
                    data.add(displayMessageRepository.save(
                        DisplayMessage(
                            sn = null,
                            messageClass = it.messageClass!!,
                            messageType = it.messageType!!,
                            colorCode = it.colorCode,
                            order = it.order,
                            messageDesc = it.messageDesc,
                            lineNumber = it.line
                        ))
                    )
                }
            }
            // static upload
            initalizeData()
            return CommonResult.data(data)
        } catch (e: RuntimeException) {
            logger.error("set display color error {} ", e.message)
            return CommonResult.error("parkinglot display setting failed ")
        }
    }

    fun updateDisplayMessage(request: ArrayList<ReqCreateMessage>): CommonResult = with(request) {
        logger.info { "updateDisplayMessage request $request" }
        try {
            request.forEach { it ->
                displayMessageRepository.findBySn(it.sn!!)?.let { display ->
                    display.messageClass = it.messageClass
                    display.messageType = it.messageType
                    display.colorCode = it.colorCode
                    display.order = it.order
                    display.messageDesc = it.messageDesc
                    display.lineNumber = it.lineNumber
                    displayMessageRepository.save(display)
                }
            }
            initalizeData()
            return CommonResult.data("display message setting success")
        }catch (e: CustomException){
            logger.error("update display message error {} ", e)
            return CommonResult.error("parkinglot display message update failed ")
        }
    }

    fun updateDisplayInfo(request: reqDisplayInfo) : DisplayInfo? {
        logger.info { "updateDisplayInfo request $request" }
        try {
            displayInfoRepository.findBySn(1)?.let { info ->
                info.line1Status = request.line1Status
                info.line2Status = request.line2Status
                return displayInfoRepository.saveAndFlush(info)
            }?: kotlin.run {
                return null
            }
        }catch (e: CustomException){
            logger.error{"update display info error $e"}
            return null
        }
    }

    fun getDisplayColor() : CommonResult {
        logger.info { "getDisplayColor" }
        try {
            displayColorRepository.findAll().let { it ->
                return CommonResult.data(it)
            }
        }catch (e: RuntimeException){
            logger.error { "getDisplayColor error ${e.message}" }
            return CommonResult.error("getDisplayColor error")
        }
    }

    fun getDisplayMessage() : CommonResult {
        logger.info { "getDisplayMessage" }
        try {
            displayMessageRepository.findAll().let { it ->
                return CommonResult.data(it)
            }
        }catch (e: RuntimeException){
            logger.error { "getDisplayColor error ${e.message}" }
            return CommonResult.error("getDisplayColor error")
        }
    }

    fun getDisplayInfo() : DisplayInfo?  {
        try {
            return displayInfoRepository.findBySn(1)?.let { it ->
                 it
            }?: kotlin.run {
                null
            }
        }catch (e: RuntimeException){
            logger.error { "getDisplayColor error $e" }
            return null
        }
    }


    private fun getRelaySvrUrl(gateId: String): String {
        return gates.filter { it.gateId == gateId }[0].relaySvr!!
    }

    fun getGateByGateId(gateId: String) : GateDTO? {
        return gates.filter { it.gateId == gateId }[0]
    }

    fun sendPaystation(data: Any, gate: String, requestId: String, type: String) {
        logger.info { "sendPaystation request $data $gate $requestId $type" }
        //todo 정산기 api 연계 개발
        parkinglotService.getFacilityByGateAndCategory(gate, FacilityCategoryType.PAYSTATION)?.let { its ->
            its.forEach {
                restAPIManager.sendPostRequest(
                    getRelaySvrUrl(gate)+"/parkinglot/paystation",
                    reqPaystation(dtFacilityId = it.dtFacilitiesId, data = setPaystationRequest(type, requestId, data))
                )
            }
        }

    }

    @Throws(CustomException::class)
    fun sendPayment(request: reqApiTmapCommon, facilitiesId: String): CommonResult? {
        logger.info { "sendPayment request $request" }
        try{
            var fileName: String? = null
            var fileUploadId: String? = null
//            val contents = JSONUtil.getJSONObject(request.contents.toString()) as reqPayStationData
            val data = JSONUtil.getJsObject(request.contents)
            val contents = readValue(data.toString(), reqPayStationData::class.java) as reqPayStationData
            // park_out update
            inoutService.updatePayment(
                reqUpdatePayment(
                    parkTicketAmount = contents.parkTicketAmount!!.toInt(),
                    paymentAmount = contents.cardAmount!!.toInt(),
                    sn = request.requestId!!.toLong(),
                    cardtransactionId = contents.transactionId!!,
                    cardNumber = contents.cardNumber,
                    approveDateTime = contents.approveDatetime
                )
            )?.let { it ->
                // gate open
                relayService.actionGate(it.gateId!!, "GATE", "open")
                // todo tmap-payment
                tmapSendService.sendTmapInterface(
                    reqSendPayment(
                        vehicleNumber = it.vehicleNo!!,
                        chargingId = it.chargingId!!,
                        paymentMachineType = "EXIT",
                        transactionId = it.cardtransactionid!!,
                        paymentType = "CARD",
                        paymentAmount = it.payfee!!
                    ),
                    parkSiteInfoService.generateRequestId(),
                    "payment"
                )
                fileName = it.image!!
                fileUploadId = it.fileuploadid
            }
            val requestId = parkSiteInfoService.generateRequestId()
            // todo tmap-outvehicle
            tmapSendService.sendOutVehicle(
                reqOutVehicle(
                    gateId = parkinglotService.getGateInfoByFacilityId(facilitiesId)!!.udpGateid!!,
                    seasonTicketYn = "N",
                    vehicleNumber = contents.vehicleNumber,
                    recognitionType = FacilityCategoryType.LPR,
                    recognitorResult = "RECOGNITION",
                    fileUploadId = fileUploadId!!
                ),
                requestId,
                fileName = fileName
            )


        }catch (e: RuntimeException) {
            logger.error("sendPayment {} ", e.message)
            return CommonResult.error("sendPayment failed ")
        }
        return null
    }

    fun setPaystationRequest(type: String, requestId: String?, contents: Any) : reqApiTmapCommon {
        return reqApiTmapCommon(
            type = type,
            parkingSiteId = parkSiteInfoService.getParkSiteId()!!,
            requestId = requestId?.let { requestId },
            eventDateTime = DateUtil.stringToNowDateTime(),
            contents = contents
        )
    }

    fun createFacility(facility: Facility): CommonResult {
        logger.info{"createFacility request : $facility"}
        try {
            return CommonResult.data(facilityRepository.save(facility))
        } catch (e: RuntimeException) {
            logger.error { "createFacility error $e" }
            return CommonResult.error("createFacility failed ")
        }
    }

    fun allUpdateFacilities(request: reqUpdateFacilities): CommonResult {
        logger.info("allUpdateFacilities request : $request")
        try {
            request.facilities.forEach {
                facilityRepository.save(it)
            }
            return CommonResult.data(facilityRepository.findAll())
        } catch (e: RuntimeException) {
            logger.error { "allUpdateFacilities error ${e.message}" }
            return CommonResult.error("allUpdateFacilities failed ")
        }
    }

    fun <T : Any> readValue(any: String, valueType: Class<T>): T {
        val factory = JsonFactory()
        factory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
        return jacksonObjectMapper().readValue(any, valueType)
    }

//    /* udp gate id */
//    fun getUdpGateId(gateId: String) {
//        val udpGateId = gateService.findOne(gateId)
//            .ifPresentOrElse(
//                { it.udpGateid },
//                { null }
//            )
//        return udpGateId
//    }

    fun updateHealthCheck(dtFacilitiesId: String, status: String) {
        logger.trace { "updateHealthCheck facility $dtFacilitiesId status $status" }
        try {
            facilityRepository.findByDtFacilitiesId(dtFacilitiesId)?.let { facility ->
                facility.health = status
                facility.healthDate = LocalDateTime.now()
                facilityRepository.saveAndFlush(facility)
            }
        }catch (e: RuntimeException) {
            logger.error { "updateHealthCheck error ${e.message}" }
        }
    }

    fun updateStatusCheck(dtFacilitiesId: String, status: String) : Facility? {
        logger.trace { "updateStatusCheck facility $dtFacilitiesId status $status" }
        try {
            facilityRepository.findByDtFacilitiesId(dtFacilitiesId)?.let { facility ->
//                if (facility.category == "BREAKER") {
                    facility.status = status
                    facility.statusDate = LocalDateTime.now()
                    return facilityRepository.save(facility)
//                }
            }
        }catch (e: RuntimeException) {
            logger.error { "updateStatusCheck error ${e.message}" }
        }
        return null
    }

    fun getStatusByGateAndCategory(gateId: String, category: FacilityCategoryType): HashMap<String, Any?>? {
        try {
            var result = HashMap<String, Any?>()
            facilityRepository.findByGateIdAndCategoryAndDelYn(gateId, category, DelYn.N)?.let { facilities ->
                val total = facilities.filter {
                    it.lprType != LprTypeStatus.ASSIST
                }
                if (total.isEmpty()) {
                    return null
                }
                val normal = facilities.filter {
                    it.health == "NORMAL"
                }
                when (normal.size) {
                    total.size -> {
                        result = hashMapOf(
                            "category" to category,
                            "status" to "NORMAL")
                    }
                    0 -> {
                        result = hashMapOf(
                            "category" to category,
                            "status" to "NORESPONSE")
                    }
                    else -> {
                        result = hashMapOf(
                            "category" to category,
                            "status" to "PARTNORMAL")
                    }
                }
            }
            return result
        }catch (e: RuntimeException) {
            logger.error { "getStatusByGateAndCategory error $e" }
        }
        return null
    }

    fun getStatusByGateAndCategoryAndLprType(gateId: String, category: FacilityCategoryType, lprType: LprTypeStatus): HashMap<String, Any?>? {
        var result = HashMap<String, Any?>()
        facilityRepository.findByGateIdAndCategoryAndDelYnAndLprType(gateId, category, DelYn.N, lprType)?.let { facilities ->
            val total = facilities.filter {
                it.lprType != LprTypeStatus.ASSIST
            }
            if (total.isEmpty()) {
                return null
            }
            val normal = facilities.filter {
                it.health == "NORMAL"
            }
            when (normal.size) {
                total.size -> {
                    result = hashMapOf(
                        "category" to category,
                        "status" to "NORMAL")
                }
                0 -> {
                    result = hashMapOf(
                        "category" to category,
                        "status" to "NORESPONSE")
                }
                else -> {
                    result = hashMapOf(
                        "category" to category,
                        "status" to "PARTNORMAL")
                }
            }
        }
        return result
    }

    fun getActionByGateAndCategory(gateId: String, category: FacilityCategoryType): HashMap<String, Any?>? {
        try {
            var result = HashMap<String, Any?>()
            facilityRepository.findByGateIdAndCategoryAndDelYn(gateId, category, DelYn.N)?.let { facilities ->
                if (facilities.isNullOrEmpty()) return result
                facilities.forEach { facility ->
                    val id = facility.facilitiesId ?: run { facility.dtFacilitiesId }

                    // 장애 상태 확인
                    failureRepository.findTopByFacilitiesIdAndExpireDateTimeIsNullOrderByIssueDateTimeDesc(id)?.let {
                        return hashMapOf(
                            "category" to category,
                            "status" to facility.status,
                            "failure" to it.failureCode)
                    }?: run {
                        result = hashMapOf(
                            "category" to category,
                            "status" to facility.status,
                            "failure" to null)
                    }

                }
            }
             return result
        }catch (e: RuntimeException) {
            logger.error { "getActionByGateAndCategory error $e" }
        }
        return null
    }

    fun getStatusByGate(gateId: String): HashMap<String, Any?>? {
        try {
            //LPR
            val lpr = getStatusByGateAndCategory(gateId, FacilityCategoryType.LPR)

            //BREAKER
            val breaker = getStatusByGateAndCategory(gateId, FacilityCategoryType.BREAKER)
            val breakerAction = getActionByGateAndCategory(gateId, FacilityCategoryType.BREAKER)

            //DISPLAY
            val display = getStatusByGateAndCategory(gateId, FacilityCategoryType.DISPLAY)

            //PAYSTATION
            val paystation = getStatusByGateAndCategory(gateId, FacilityCategoryType.PAYSTATION)
            val paystationAction = getActionByGateAndCategory(gateId, FacilityCategoryType.PAYSTATION)

            logger.debug { "breaker status ${breaker!!.get("status")} action ${breakerAction!!.get("status")}" }

            return hashMapOf<String, Any?>(
                "lprStatus" to if (lpr==null) "NONE" else  lpr["status"],
                "breakerStatus" to if (breaker==null) "NONE" else breaker["status"],
                "breakerAction" to if (breakerAction==null) "NONE" else breakerAction["status"],
                "breakerFailure" to if (breakerAction==null) null else breakerAction["failure"],
                "displayStatus" to if (display==null) "NONE" else display["status"],
                "paystationStatus" to if (paystation==null) "NONE" else paystation["status"],
                "paystationAction" to if (paystationAction==null) null else paystationAction["status"],
                "paystationFailure" to if (paystationAction==null) null else paystationAction["failure"]
            )

        }catch (e: CustomException){
            logger.error { "getStatusByGate error ${e.message}" }
        }
        return null
    }

    fun getStatusByINOUTGate(gateId: String): HashMap<String, Any?>? {
        try {
            // 입구, 출구 LPR
            val inLpr = getStatusByGateAndCategoryAndLprType(gateId, FacilityCategoryType.LPR, LprTypeStatus.INFRONT)
            val outLpr = getStatusByGateAndCategoryAndLprType(gateId, FacilityCategoryType.LPR, LprTypeStatus.OUTFRONT)

            //BREAKER
            val breaker = getStatusByGateAndCategory(gateId, FacilityCategoryType.BREAKER)
            val breakerAction = getActionByGateAndCategory(gateId, FacilityCategoryType.BREAKER)

            // 입구, 출구 DISPLAY
            val inDisplay = getStatusByGateAndCategoryAndLprType(gateId, FacilityCategoryType.DISPLAY, LprTypeStatus.INFRONT)
            val outDisplay = getStatusByGateAndCategoryAndLprType(gateId, FacilityCategoryType.DISPLAY, LprTypeStatus.OUTFRONT)

            //PAYSTATION
            val paystation = getStatusByGateAndCategory(gateId, FacilityCategoryType.PAYSTATION)
            val paystationAction = getActionByGateAndCategory(gateId, FacilityCategoryType.PAYSTATION)

            logger.debug { "breaker status ${breaker!!["status"]} action ${breakerAction!!["status"]}" }

            return hashMapOf<String, Any?>(
                "inLprStatus" to if (inLpr==null) "NONE" else  inLpr["status"],
                "outLprStatus" to if (outLpr==null) "NONE" else  outLpr["status"],
                "breakerStatus" to if (breaker==null) "NONE" else breaker["status"],
                "breakerAction" to if (breakerAction==null) "NONE" else breakerAction["status"],
                "breakerFailure" to if (breakerAction==null) null else breakerAction["failure"],
                "inDisplayStatus" to if (inDisplay==null) "NONE" else inDisplay["status"],
                "outDisplayStatus" to if (outDisplay==null) "NONE" else outDisplay["status"],
                "paystationStatus" to if (paystation==null) "NONE" else paystation["status"],
                "paystationAction" to if (paystationAction==null) null else paystationAction["status"],
                "paystationFailure" to if (paystationAction==null) null else paystationAction["failure"]
            )

        }catch (e: CustomException){
            logger.error { "getStatusByGate error ${e.message}" }
        }
        return null
    }

    fun getOneFacilityByGateIdAndCategory(gateId: String, category: FacilityCategoryType): Facility? {
        return facilityRepository.findByGateIdAndCategoryAndDelYn(gateId, category, DelYn.N)?.let { list ->
            list[0]
        }
    }

    fun activeGateFacilities(): List<FacilityDTO> {
        var result = ArrayList<FacilityDTO>()
        gateService.findActiveGate().let { gates ->
            for (gate in gates.filter { g -> g.delYn == DelYn.N }) {
                facilityRepository.findByGateIdAndDelYn(gate.gateId!!, DelYn.N)?.let { facilities ->
                    for (facility in facilities) {
                        result.add(facilityMapper.toDTO(facility)
                        )
                    }
                }
            }
        }
        return result
    }

    fun allFacilities(): List<FacilityDTO> {
        var result = ArrayList<FacilityDTO>()
        gateService.findAll().let { gates ->
            for (gate in gates) {
                facilityRepository.findByGateId(gate.gateId!!)?.let { facilities ->
                    for (facility in facilities) {
                        result.add(facilityMapper.toDTO(facility)
                        )
                    }
                }
            }
        }
        return result
    }

    fun getGateByFacilityId(dtFacilityId: String) : Facility? {
        return facilityRepository.findByDtFacilitiesId(dtFacilityId)
    }

    fun save(facilityDTO: FacilityDTO) : FacilityDTO {
        var facility = facilityMapper.toEntity(facilityDTO)
        facility = facilityRepository.saveAndFlush(facility!!)
        return facilityMapper.toDTO(facility)
    }

    fun findByGateId(gateId: String): List<FacilityDTO>? {
        return facilityRepository.findByGateId(gateId)?.map(facilityMapper::toDTO) ?: kotlin.run { null }
    }
}
