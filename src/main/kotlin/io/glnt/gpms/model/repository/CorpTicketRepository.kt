package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.CorpTicket
import io.glnt.gpms.model.entity.DiscountClass
import io.glnt.gpms.model.enums.DelYn
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface CorpTicketRepository: JpaRepository<CorpTicket, Long> {
    fun findByCorpId(corpId: String) : List<CorpTicket>?
}

@Repository
interface DiscountClassRepository: JpaRepository<DiscountClass, Long>{
    fun findByExpireDateGreaterThanEqualAndEffectDateLessThanEqualAndDelYn(start: LocalDateTime, end: LocalDateTime, delYn: DelYn): List<DiscountClass>?
    fun findByDelYn(delYn: DelYn): List<DiscountClass>?
}