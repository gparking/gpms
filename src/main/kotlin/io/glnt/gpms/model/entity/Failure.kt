package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.entity.Auditable
import org.springframework.format.annotation.DateTimeFormat
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_failure")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Failure(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Column(name = "issueDateTime", nullable = false)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var issueDateTime: LocalDateTime,

    @Column(name = "expireDateTime", nullable = true)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var expireDateTime: LocalDateTime? = null,

    @Column(name = "FacilitiesId", nullable = true)
    var facilitiesId: String? = null,

    @Column(name = "f_name", nullable = true)
    var fName: String? = null,

    @Column(name = "failureCode", nullable = true)
    var failureCode: String? = null,

    @Column(name = "failureType", nullable = true)
    var failureType: String? = null,

    @Column(name = "failureFlag", nullable = true)
    var failureFlag: Int? = 0
): Auditable(), Serializable {

}
