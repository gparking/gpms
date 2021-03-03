package io.glnt.gpms.handler.dashboard.admin.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.facility.service.FacilityService
import io.glnt.gpms.handler.inout.model.reqSearchParkin
import io.glnt.gpms.handler.inout.service.InoutService
import io.glnt.gpms.handler.parkinglot.model.reqSearchParkinglotFeature
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.relay.service.RelayService
import io.glnt.gpms.model.entity.Gate
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DashboardAdminService {
    companion object : KLogging()

    @Autowired
    lateinit var inoutService: InoutService

    @Autowired
    lateinit var parkinglotService: ParkinglotService

    @Autowired
    lateinit var facilityService: FacilityService

    @Autowired
    lateinit var relayService: RelayService


    @Throws(CustomException::class)
    fun getMainGates(): CommonResult {
        try {
            val result = ArrayList<HashMap<String, Any?>>()

            val gates = parkinglotService.getParkinglotGates(reqSearchParkinglotFeature())
            when (gates.code) {
                ResultCode.SUCCESS.getCode() -> {
                    gates.data?.let {
                        val lists = gates.data as List<Gate>
                        lists.forEach {
                            // gate 당 입출차 내역 조회
                            val inout = inoutService.getLastInout(it.gateType, it.gateId)
                            // 각 장비 상태 조회
                            val gateStatus = facilityService.getStatusByGate(it.gateId)
                            result.add(hashMapOf<String, Any?>(
                                "gateId" to it.gateId,
                                "gateName" to it.gateName,
                                "gateType" to it.gateType,
                                "image" to inout!!["image"],
                                "vehicleNo" to inout["vehicleNo"],
                                "date" to inout["date"],
                                "carType" to inout["carType"],
                                "breakerAction" to gateStatus!!["breakerAction"],
                                "breakerStatus" to gateStatus["breakerStatus"],
                                "displayStatus" to gateStatus["displayStatus"],
                                "paystationStatus" to gateStatus["paystationStatus"],
                                "paystationAction" to gateStatus["paystationAction"],
                                "lprStatus" to gateStatus["lprStatus"]
                            ))
                        }
                    }
                }
            }
            return CommonResult.data(result)
        }catch (e: CustomException){
            logger.error { "Admin getMainGates failed ${e.message}" }
            return CommonResult.error("Admin getMainGates failed ${e.message}")
        }
    }

    @Throws(CustomException::class)
    fun getParkInLists(request: reqSearchParkin): CommonResult {
        try {
            return CommonResult.data(inoutService.getAllParkLists(request).data)
        }catch (e: CustomException){
            logger.error { "Admin getParkInLists failed ${e.message}" }
            return CommonResult.error("Admin getParkInLists failed ${e.message}")
        }
    }

    @Throws(CustomException::class)
    fun gateAction(action: String, gateId: String) : CommonResult {
        try {
            relayService.actionGate(gateId, "GATE", action)
            return CommonResult.data()
        }catch (e: CustomException){
            logger.error { "Admin gateAction failed ${e.message}" }
            return CommonResult.error("Admin gateAction failed ${e.message}")
        }
    }


}