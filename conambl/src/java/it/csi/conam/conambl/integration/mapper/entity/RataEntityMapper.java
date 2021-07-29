/*******************************************************************************
 * SPDX-License-Identifier: EUPL-1.2
 * Copyright Regione Piemonte - 2020
 ******************************************************************************/
package it.csi.conam.conambl.integration.mapper.entity;

import it.csi.conam.conambl.integration.entity.CnmTRata;
import it.csi.conam.conambl.integration.mapper.EntityMapper;
import it.csi.conam.conambl.vo.pianorateizzazione.RataVO;

/**
 * @author riccardo.bova
 * @date 15 gen 2019
 */
public interface RataEntityMapper extends EntityMapper<CnmTRata, RataVO> {

	CnmTRata mapVOtoEntityUpdate(RataVO vo, CnmTRata cnmTRata);

}
