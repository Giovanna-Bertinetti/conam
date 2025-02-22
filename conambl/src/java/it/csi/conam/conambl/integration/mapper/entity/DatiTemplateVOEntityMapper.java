/*******************************************************************************
 * SPDX-License-Identifier: EUPL-1.2
 * Copyright Regione Piemonte - 2020
 ******************************************************************************/
package it.csi.conam.conambl.integration.mapper.entity;

import it.csi.conam.conambl.integration.entity.CnmRVerbaleSoggetto;
import it.csi.conam.conambl.integration.entity.CnmTOrdinanza;
import it.csi.conam.conambl.integration.entity.CnmTPianoRate;
import it.csi.conam.conambl.integration.entity.CnmTSollecito;
import it.csi.conam.conambl.vo.template.DatiTemplateVO;

import java.util.List;

/**
 * @author riccardo.bova
 * @date 13 nov 2018
 */
public interface DatiTemplateVOEntityMapper {

	DatiTemplateVO mapEntityToVO(CnmTPianoRate dto);

	DatiTemplateVO mapEntityToVO(CnmTOrdinanza cnmTOrdinanza);

	DatiTemplateVO mapEntityToVO(CnmTSollecito cnmTSollecito);

	DatiTemplateVO mapEntityToVO(List<CnmRVerbaleSoggetto> cnmRVerbaleSoggettoList);
}
