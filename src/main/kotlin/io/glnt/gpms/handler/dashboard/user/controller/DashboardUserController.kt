package io.glnt.gpms.handler.dashboard.user.controller

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.handler.dashboard.user.model.*
import io.glnt.gpms.handler.dashboard.user.service.DashboardUserService
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}/dashboard"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class DashboardUserController {
    @Autowired
    private lateinit var dashboardUserService: DashboardUserService

    @RequestMapping(value=["/parking/discount/search/vehicle"], method = [RequestMethod.POST])
    fun parkingDiscountSearchVehicle(@RequestBody request: reqVehicleSearch): ResponseEntity<CommonResult> {
        logger.info { "parkingDiscountSearchVehicle $request" }
        return returnResult(dashboardUserService.parkingDiscountSearchVehicle(request))
    }

    fun returnResult(result: CommonResult): ResponseEntity<CommonResult> {
        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.CREATED.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
        }
    }


    companion object : KLogging()
}