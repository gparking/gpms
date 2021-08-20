package io.glnt.gpms.model.dto

import io.glnt.gpms.model.entity.Gate
import io.glnt.gpms.model.entity.GateGroup
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.GateTypeStatus
import io.glnt.gpms.model.enums.OpenActionType
import java.io.Serializable
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class GateDTO(
    var sn: Long?,

    @get: NotNull
    var gateName: String? = null,

    @get: NotNull
    var gateId: String? = null,

    @Enumerated(EnumType.STRING)
    var gateType: GateTypeStatus? = null,

    var takeAction: String? = null,

    var udpGateid: String? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var openAction: OpenActionType? = null,

    var relaySvrKey: String? = null,

    var relaySvr: String? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var delYn: DelYn? = null,

    var resetSvr: String? = null,

    var gateGroupId: String? = null,

    var gateGroup: GateGroupDTO? = null,
) : Serializable {
    constructor(gate: Gate) :
        this(
            gate.sn, gate.gateName, gate.gateId, gate.gateType, gate.takeAction, gate.udpGateid,
            gate.openAction, gate.relaySvrKey, gate.relaySvr, gate.delYn, gate.resetSvr, gate.gateGroupId
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GateDTO) return false
        return gateId != null && gateId == other.gateId
    }

    override fun hashCode() = 31
}