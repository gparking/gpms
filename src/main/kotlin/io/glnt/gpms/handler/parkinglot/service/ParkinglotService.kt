package io.glnt.gpms.handler.parkinglot.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.utils.Base64Util
import io.glnt.gpms.common.utils.OkHttpClientUtils
import io.glnt.gpms.handler.parkinglot.model.reqSearchParkinglotFeature
import io.glnt.gpms.handler.parkinglot.model.reqAddParkinglotFeature
import io.glnt.gpms.handler.product.service.ProductService
import io.glnt.gpms.handler.parkinglot.model.reqAddParkIn
import io.glnt.gpms.common.utils.DataCheckUtil
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.entity.ParkSiteInfo
import io.glnt.gpms.model.repository.ParkSiteInfoRepository
import io.glnt.gpms.model.entity.ParkFeature
import io.glnt.gpms.model.entity.ParkIn
import io.glnt.gpms.model.repository.ParkFacilityRepository
import io.glnt.gpms.model.repository.ParkFeatureRepository
import io.glnt.gpms.model.repository.ParkInRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.io.File
import java.lang.RuntimeException
import java.time.LocalDate
import java.util.*

@Service
class ParkinglotService {
    companion object : KLogging()

    lateinit var parkSite: ParkSiteInfo

    @Autowired
    lateinit var enviroment: Environment

    @Autowired
    lateinit var productService: ProductService

    @Autowired
    private lateinit var parkFeatureRepository: ParkFeatureRepository

    @Autowired
    private lateinit var parkFacilityRepository: ParkFacilityRepository

    @Autowired
    private lateinit var parkSiteInfoRepository: ParkSiteInfoRepository

    @Autowired
    private lateinit var parkInRepository: ParkInRepository

    fun addParkinglotFeature(request: reqAddParkinglotFeature): CommonResult = with(request) {
        logger.debug("addParkinglotFeature service {}", request)
        try {
            val new = ParkFeature(
                idx = null,
                featureId = featureId,
                flag = flag,
                groupKey = groupKey,
                category = category,
                connectionType = connetionType,
                ip = ip,
                port = port,
                originImgPath = path,
                transactinoId = transactionId
            )
            parkFeatureRepository.save(new)
            return CommonResult.created("parkinglot feature add success")
        } catch (e: RuntimeException) {
            logger.error("addParkinglotFeature error {} ", e.message)
            return CommonResult.error("parkinglot feature db add failed ")
        }
    }

    fun getParkinglotFeature(requet: reqSearchParkinglotFeature): CommonResult {
        requet.featureId?.let {
            val list = parkFeatureRepository.findByFeatureId(it)
            return if (list == null) CommonResult.notfound("parkinglot feature") else CommonResult.data(list)
        } ?: run {
            requet.gateSvrKey?.let {
                val lists = parkFeatureRepository.findByGroupKey(it)
                return if (lists.isNullOrEmpty()) CommonResult.notfound("parkinglot feature") else CommonResult.data(lists)
            } ?: run {
                parkFeatureRepository.findAll().let {
                    return CommonResult.data(it)
                }
            }
        }
    }

    fun getParkinglotfacilities(requet: reqSearchParkinglotFeature): CommonResult {
        requet.facilitiesId?.let {
            val list = parkFacilityRepository.findByFacilitiesId(it)
            return if (list == null) CommonResult.notfound("parkinglot facilities") else CommonResult.data(list)
        } ?: run {
            requet.gateSvrKey?.let {
                val lists = parkFacilityRepository.findByGateSvrKey(it)
                return if (lists.isNullOrEmpty()) CommonResult.notfound("parkinglot facilities") else CommonResult.data(lists)
            } ?: run {
                parkFacilityRepository.findAll().let {
                    return CommonResult.data(it)
                }
            }
        }
    }

    fun parkIn(request: reqAddParkIn) : CommonResult = with(request){
        logger.debug("parkIn service {}", request)
        try {
            //todo gate up(option check)

            // image 파일 저장
            if (base64Str != null) {
                // folder check
                fileFullPath = enviroment.getProperty("image.filepath")+"/"+ LocalDate.now()
                File(fileFullPath).apply {
                    if (!exists()) {
                        mkdirs()
                    }
                }
                fileName = parkSite.parkId+"_"+parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.udpGateid+"_"+DateUtil.nowTimeDetail.substring(9,12)+vehicleNo+".jpg"
                fileUploadId = DateUtil.stringToNowDateTimeMS()+"_F"
                val imageByteArray = Base64Util.decodeAsBytes(base64Str!!)
                if (imageByteArray != null) {
                    File("$fileFullPath/$fileName").writeBytes(imageByteArray)
                }
            }

            //차량번호 패턴 체크
            if (DataCheckUtil.isValidCarNumber(vehicleNo)) {
                //todo 출차 정보 확인 후 update

                parkingtype = "일반차량"
                //todo 정기권 차량 여부 확인
                productService.getValidProductByVehicleNo(vehicleNo)?.let {
                    parkingtype = "정기차량"
                    validDate = it.validDate
                }
            } else {
                parkingtype = "미인식차량"
            }
            requestId = DataCheckUtil.generateRequestId(parkSite.parkId!!)

            //todo 입차 정보 DB insert
            val newData = ParkIn(
                sn = null,
                gateId = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.gateId,
                parkcartype = parkingtype,
                userSn = 0,
                vehicleNo = vehicleNo,
                image = "$fileFullPath/$fileName",
                flag = 1,
                validate = validDate,
                resultcode = resultcode.toInt(),
                requestid = requestId,
                fileuploadid = fileUploadId,
                hour = DateUtil.nowTimeDetail.substring(0,2),
                min = DateUtil.nowTimeDetail.substring(3,5),
                inDate = inDate
            )
            parkInRepository.save(newData)

            //todo tmap 전송
//            var tmapSend = enviroment.getProperty("tmap.send")

//        if (tmapSend.equals("on")) {
//            OkHttpClientUtils.postJson()
//        }
            CommonResult.created()

        } catch (e: RuntimeException) {
            logger.error("addParkinglotFeature error {} ", e.message)
            return CommonResult.error("parkinglot feature db add failed ")
        }


    }

    fun getParkSiteInfo() {
        parkSiteInfoRepository.findTopByOrderBySiteid()?.let {
            parkSite = it
        }
    }
}