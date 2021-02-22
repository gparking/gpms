package io.glnt.gpms.handler.discount.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.discount.model.availableTicketClass
import io.glnt.gpms.handler.discount.model.reqDiscountableTicket
import io.glnt.gpms.handler.discount.model.reqSearchDiscount
import io.glnt.gpms.handler.discount.model.reqSearchInoutDiscount
import io.glnt.gpms.model.entity.CorpTicket
import io.glnt.gpms.model.entity.DiscountClass
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.DiscountRangeType
import io.glnt.gpms.model.repository.CorpTicketRepository
import io.glnt.gpms.model.repository.DiscountClassRepository
import io.glnt.gpms.model.repository.InoutDiscountRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DiscountService {
    companion object : KLogging()

    @Autowired
    private lateinit var discountClassRepository: DiscountClassRepository

    @Autowired
    private lateinit var corpTicketRepository: CorpTicketRepository

    @Autowired
    private lateinit var inoutDiscountRepository: InoutDiscountRepository

    fun getDiscountClass() : CommonResult {
        return CommonResult.data(discountClassRepository.findAll())
    }

    fun getByCorp(request: reqSearchDiscount) : CommonResult {
        request.corpId?.let {
            val lists = corpTicketRepository.findByCorpId(it)
            return if (lists.isNullOrEmpty()) CommonResult.notfound("corp ticket") else CommonResult.data(lists)
        } ?: run {
            corpTicketRepository.findAll().let {
                return CommonResult.data(it)
            }
        }
    }

    fun getDiscountableTicketsByCorp(corp: String) {
        return
    }

    fun createDiscountClass(request: DiscountClass): CommonResult {
        logger.info { "createDiscountClass $request" }
        try {
            discountClassRepository.save(request)
            return CommonResult.data(getDiscountClass())
        }catch (e: RuntimeException) {
            logger.error { "createDiscountClass error ${e.message}" }
            return CommonResult.Companion.error("tb_corpclass create failed")
        }
    }

    fun getDiscountableTickets(request: reqDiscountableTicket): CommonResult {
        logger.info { "getDiscountableTickets $request" }
        try {
            val weekRange = DateUtil.getWeekRange(DateUtil.formatDateTime(request.date, "yyyy-MM-dd"))
            val tickets = getByCorp(reqSearchDiscount(corpId = request.corpId))
            if (tickets.code == ResultCode.SUCCESS.getCode() && tickets.data != null) {
                val data = ArrayList<CorpTicket>()
                for (ticket in tickets.data as List<CorpTicket>) {
                    ticket.ableCnt = getInoutDiscount(reqSearchInoutDiscount(ticketSn = ticket.discountClass!!.sn!!, inSn = request.inSn))
                    if (ticket.ableCnt != null) {
                        if (ticket.ableCnt!! > 0 ) data.add(ticket)
                    }
                }

                val result = data.filter {
                    // 입차 요일 확인
                    ( it.discountClass!!.range1 == weekRange || it.discountClass!!.range1 == DiscountRangeType.ALL)
                }
                if (result.isNullOrEmpty()) return CommonResult.notfound("")
                return CommonResult.data(result)
            }
            return CommonResult.notfound("")
        }catch (e: CustomException){
            logger.error { "getDiscountableTickets error ${e.message}" }
            return CommonResult.error("getDiscountableTickets search failed")
        }
    }

    fun getInoutDiscount(request: reqSearchInoutDiscount) : Int? {
        logger.info { "getInoutDiscount $request" }
        try {
            val result = ArrayList<Int>()
            discountClassRepository.findBySn(request.ticketSn).let { discountClass ->
                var no = inoutDiscountRepository.findByTicketSnAndInSnAndDelYn(request.ticketSn, request.inSn, DelYn.N)?.sumBy { it -> it.quantity!! }.also {
                    if (it!! > discountClass.disMaxNo!!) return 0
                    if (it == 0) result.add(discountClass.disMaxNo!!) else  result.add(it)
                }
                var day = inoutDiscountRepository.findByTicketSnAndCreateDateGreaterThanEqualAndCreateDateLessThanEqualAndDelYn(
                    request.ticketSn, DateUtil.beginTimeToLocalDateTime(DateUtil.nowDate), DateUtil.lastTimeToLocalDateTime(DateUtil.nowDate), DelYn.N )?.sumBy { it -> it.quantity!! }.also {
                    if (it!! > discountClass.disMaxDay!!) return 0
                    if (it == 0) result.add(discountClass.disMaxDay!!) else  result.add(it)
                }
                val month = inoutDiscountRepository.findByTicketSnAndCreateDateGreaterThanEqualAndCreateDateLessThanEqualAndDelYn(
                    request.ticketSn, DateUtil.firstDayToLocalDateTime(DateUtil.nowDate), DateUtil.lastDayToLocalDateTime(DateUtil.nowDate), DelYn.N )?.sumBy { it -> it.quantity!! }.also {
                    if (it!! > discountClass.disMaxMonth!!) return 0
                    if (it == 0) result.add(discountClass.disMaxMonth!!) else  result.add(it)
                }
            }
            return result.min()
        }catch (e: CustomException){
            logger.error { "getInoutDiscount error ${e.message}" }
            return null//CommonResult.error("getInoutDiscount search failed")
        }
    }

}