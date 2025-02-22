/*******************************************************************************
 * SPDX-License-Identifier: EUPL-1.2
 * Copyright Regione Piemonte - 2020
 ******************************************************************************/
package it.csi.conam.conambl.integration.doqui.helper.impl;

import it.csi.conam.conambl.business.service.util.UtilsDate;
import it.csi.conam.conambl.common.security.SecurityUtils;
import it.csi.conam.conambl.integration.beans.*;
import it.csi.conam.conambl.integration.doqui.DoquiConstants;
import it.csi.conam.conambl.integration.doqui.bean.*;
import it.csi.conam.conambl.integration.doqui.entity.*;
import it.csi.conam.conambl.integration.doqui.exception.ArchiviaDocumentoException;
import it.csi.conam.conambl.integration.doqui.exception.FruitoreException;
import it.csi.conam.conambl.integration.doqui.exception.IntegrationException;
import it.csi.conam.conambl.integration.doqui.exception.ProtocollaDocumentoException;
import it.csi.conam.conambl.integration.doqui.helper.ManageArchiviaDocumentoHelper;
import it.csi.conam.conambl.integration.doqui.helper.ManageDocumentoHelper;
import it.csi.conam.conambl.integration.doqui.service.ActaManagementService;
import it.csi.conam.conambl.integration.doqui.utils.XmlSerializer;
import it.csi.conam.conambl.integration.entity.CnmTUser;
import it.csi.conam.conambl.integration.repositories.CnmTUserRepository;
import it.csi.conam.conambl.security.UserDetails;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ManageArchiviaDocumentoHelperImpl extends CommonManageDocumentoHelperImpl implements ManageArchiviaDocumentoHelper {


//	private static final String LOGGER_PREFIX = DoquiConstants.LOGGER_PREFIX + ".helper";
//	private static final Logger log = Logger.getLogger(LOGGER_PREFIX);
	private static Logger log = Logger.getLogger(ManageArchiviaDocumentoHelperImpl.class);

	@Autowired
	private ActaManagementService	actaManagementService;

	// 20200610_LC
	@Autowired
	private ManageDocumentoHelper manageDocumentoHelper;

	// 20200630_LC
	@Autowired
	private UtilsDate utilsDate;
	
	// 20200702_LC
	@Autowired
	private CnmTUserRepository cnmTUserRepository;

//	public ActaManagementService getActaManagementService() {
//		return actaManagementService;
//	}
//	public void setActaManagementService(ActaManagementService actaManagementService) {
//		this.actaManagementService = actaManagementService;
//	}



	@SuppressWarnings("unused")
	@Transactional(propagation=Propagation.REQUIRED)	
	public ResponseArchiviaDocumento archiviaDocumentoLogico(RequestArchiviaDocumentoLogico request) throws ArchiviaDocumentoException 
	{
		String method = "archiviaDocumentoLogico";
		log.debug(method + ". BEGIN");
		ResponseArchiviaDocumento response =  new ResponseArchiviaDocumento();
		boolean containsError = false;
		if(log.isDebugEnabled())
		{	
			log.debug(method + ". codice fruitore  = " + request.getCodiceFruitore());
			log.debug(method + ". folder           = " + request.getFolder());
			log.debug(method + ". tipo Documento   = " + request.getTipoDocumento());

		}
		CnmTRichiesta cnmTRichiestaN = null;
		try
		{
			// validazioni 	
			if(request == null) throw new ArchiviaDocumentoException("Request non valorizzata");
			if(request.getSoggetto() == null) throw new ProtocollaDocumentoException("Soggetto non valorizzato");
			if(request.getSoggetto().getTipologia() == null) throw new ProtocollaDocumentoException("Tipologia soggetto non valorizzato");
			if(request.getSoggetto().getCognome() == null) throw new ProtocollaDocumentoException("Cognome soggetto non valorizzato");
			if(request.getSoggetto().getNome() == null) throw new ProtocollaDocumentoException("Nome soggetto non valorizzato");
			if(StringUtils.isBlank(request.getCodiceFruitore())) throw new ArchiviaDocumentoException("Codice fruitore non presente");
			if(StringUtils.isBlank(request.getTipoDocumento())) throw new ArchiviaDocumentoException("Tipo documento non presente");
			if(StringUtils.isBlank(request.getFolder())) throw new ArchiviaDocumentoException("Folder non presente");

			// recupero fruitore
			CnmTFruitore cnmTFruitore = getFruitore();
			// tipo documento
			CnmDTipoDocumento cnmDTipoDocumento = getTipoDocumento(request.getTipoDocumento());
			//tipo fornitore
			CnmDTipoFornitore cnmDTipoFornitore = getCnmDTipoFornitoreRepository().findOne(DoquiConstants.FORNITORE_ACTA);

			// RICHIESTA
			CnmTRichiesta cnmTRichiesta = createCnmTRichiesta(cnmTFruitore.getIdFruitore(), DoquiConstants.SERVIZIO_INSERIMENTO_ARCHIVIAZIONE);


			// DOCUMENTO
			
			// 20200711_LC 
			CnmTUser cnmTUser = null;
			if (SecurityUtils.getAuthentication()==null) {
				cnmTUser = cnmTUserRepository.findByCodiceFiscaleAndFineValidita(DoquiConstants.USER_SCHEDULED_TASK);
			} else {
				UserDetails user = SecurityUtils.getUser();
				cnmTUser = cnmTUserRepository.findOne(user.getIdUser());
			}
			
			CnmTDocumento cnmTDocumento = new CnmTDocumento();
//			cnmTDocumento.setIdDocumento(getDocTDocumentoDaoIncrementer().nextLongValue());
			cnmTDocumento.setFolder(request.getFolder());
			cnmTDocumento.setCnmDTipoDocumento(cnmDTipoDocumento);	// 20200630_LC
			cnmTDocumento.setDataOraInsert(utilsDate.asTimeStamp(LocalDateTime.now()));	// 20200630_LC
			cnmTDocumento.setCnmTUser2(cnmTUser);		// 20200702_LC
			
			String targa = null;
			if(request.getMetadati() != null)
			{
//				cnmTDocumento.setCodiceFiscale(request.getMetadati().getCodiceFiscale());
				cnmTDocumento.setIdentificativoEntitaFruitore(request.getMetadati().getIdEntitaFruitore());
//				targa = request.getMetadati().getTarga();
//				cnmTDocumento.setTarga(targa);
			}

			// 20201006_LC save anticipato
			cnmTDocumento = getCnmTDocumentoRepository().save(cnmTDocumento);
			cnmTDocumento.setIdentificativoArchiviazione(componiParolaChiave(String.valueOf(cnmTDocumento.getIdDocumento())));	// 20200713_LC
			CnmTDocumento cnmTDocumentoN =getCnmTDocumentoRepository().save(cnmTDocumento);	
			cnmTRichiestaN = getStatoRichiestaService().insertRichiesta(cnmTRichiesta);
			
			//RICHIESTA DOCUMENTO

			// 20200610_LC
			CnmRRichiestaDocumentoPK cnmRRichiestaDocumentoPK = new CnmRRichiestaDocumentoPK();
			cnmRRichiestaDocumentoPK.setIdDocumento(cnmTDocumento.getIdDocumento());
			cnmRRichiestaDocumentoPK.setIdRichiesta(cnmTRichiesta.getIdRichiesta());
			
			CnmRRichiestaDocumento cnmRRichiestaDocumentoN = new CnmRRichiestaDocumento();
			cnmRRichiestaDocumentoN.setId(cnmRRichiestaDocumentoPK);
			cnmRRichiestaDocumentoN.setCnmTDocumento(cnmTDocumento);
			cnmRRichiestaDocumentoN.setCnmTRichiesta(cnmTRichiesta);

			
			// 20200731_LC nuovo metadato collocazioneCartacea
			request.setCollocazioneCartacea(cnmDTipoDocumento.getCollocazioneCartacea());
			
			

			//POJO
			UtenteActa utenteActa = new UtenteActa();
			utenteActa.setCodiceFiscale(cnmTFruitore.getCfActa());
			utenteActa.setIdAoo(new Integer(cnmTFruitore.getIdAooActa()));
			utenteActa.setIdNodo(new Integer(cnmTFruitore.getIdNodoActa()));
			utenteActa.setIdStruttura(new Integer(cnmTFruitore.getIdStrutturaActa()));
			utenteActa.setApplicationKeyActa(cnmDTipoFornitore.getApplicationKey());
			utenteActa.setRepositoryName(cnmDTipoFornitore.getRepository());
			utenteActa.setRootActa(cnmDTipoDocumento.getRoot());
			utenteActa.setIdvitalrecordcodetype(cnmDTipoDocumento.getIdVitalRecordCodeType());
			utenteActa.setIdStatoDiEfficacia(cnmDTipoDocumento.getIdStatoEfficacia());


			//POJO
			DocumentoActa documentoActa = new DocumentoActa();
			documentoActa.setIdDocumento(cnmTDocumento.getIdentificativoArchiviazione());
			documentoActa.setFolder(request.getFolder());
			documentoActa.setFruitore(cnmTFruitore.getDescrFruitore());
			documentoActa.setApplicativoAlimentante(cnmTFruitore.getDescrFruitore());
			if(request.getMetadati() != null)
			{
				MetadatiActa metadatiActa = new MetadatiActa();
				metadatiActa.setCodiceFiscale(request.getMetadati().getCodiceFiscale());
				metadatiActa.setIdEntitaFruitore(request.getMetadati().getIdEntitaFruitore());
				metadatiActa.setTarga(request.getMetadati().getTarga());
				documentoActa.setMetadatiActa(metadatiActa);
			}

			SoggettoActa soggettoActa = new SoggettoActa();
			if (Soggetto.TOPOLOGIA_SOGGETTO_MITTENTE.equals(request.getSoggetto().getTipologia()))
			{
				soggettoActa.setMittente(true);
				documentoActa.setAutore(request.getSoggetto().getCognome());	
				utenteActa.setDescFormaDocumentaria(cnmDTipoDocumento.getDescFormadocEntrata());
			}
			else
			{
				soggettoActa.setMittente(false);
				documentoActa.setAutore("");
				utenteActa.setDescFormaDocumentaria(cnmDTipoDocumento.getDescFormadocUscita());
			}
			soggettoActa.setCognome(request.getSoggetto().getCognome());
			soggettoActa.setNome(request.getSoggetto().getNome());
			if (request.getSoggetto().getDenominazione() != null)
				soggettoActa.setDenominazione(request.getSoggetto().getDenominazione());

			documentoActa.setSoggettoActa(soggettoActa);
			documentoActa.setAutoreFisico(request.getAutoreFisico());
			documentoActa.setApplicativoAlimentante(request.getApplicativoAlimentante());
			
			// 20200731_LC
			documentoActa.setCollocazioneCartacea(request.getCollocazioneCartacea());

			if(log.isDebugEnabled())
			{
				log.debug(method + ". UtenteActa =\n " + XmlSerializer.objectToXml(utenteActa));
				log.debug(method + ". DocumentoActa =\n " + XmlSerializer.objectToXml(documentoActa));
			}

			// 20201006_LC save anticipato
			//	CnmTDocumento cnmTDocumentoN =getCnmTDocumentoRepository().save(cnmTDocumento);	
			//	cnmTRichiestaN = getStatoRichiestaService().insertRichiesta(cnmTRichiesta);
			CnmRRichiestaDocumento cnmRRichiestaDocumento = getCnmRRichiestaDocumentoRepository().save(cnmRRichiestaDocumentoN);
			if(log.isDebugEnabled()){
				log.debug(method + ". inserito documento           = " + cnmTDocumentoN);
				log.debug(method + ". inserita richiesta           = " + cnmTRichiestaN);
				log.debug(method + ". inserita richiesta documento = " + cnmRRichiestaDocumento);

			}

			KeyDocumentoActa keyDocumentoActa = actaManagementService.archiviaDocumentoLogico(documentoActa, utenteActa);
			log.debug(method + ". KeyDocumentoActa =\n " + XmlSerializer.objectToXml(keyDocumentoActa));

			if (keyDocumentoActa != null)
			{
				changeRiferimentiFornitoreDocumento(cnmTDocumento.getIdDocumento(), keyDocumentoActa.getUUIDDocumento(), keyDocumentoActa.getNumeroProtocollo());
				getStatoRichiestaService().changeStatoRichiesta(cnmTRichiesta.getIdRichiesta(), DoquiConstants.STATO_RICHIESTA_EVASA);

				//response.setIdDocumento(keyDocumentoActa.getParolaChiave());
				response.setIdDocumento(String.valueOf(cnmTDocumento.getIdDocumento()));	// 20200713_LC
			}
			else
				containsError = true;

			return response;

		}
		catch (FruitoreException e)
		{
			containsError = true;
			log.error(method + ". FruitoreException: " + e);
			throw new ArchiviaDocumentoException(e.getMessage());
		}
		catch (IntegrationException e) 
		{
			containsError = true;
			log.error(method + ". IntegrationException: " + e);
			throw new ArchiviaDocumentoException(e.getMessage());
		}
		catch (Exception e) 
		{
			containsError = true;
			log.error(method + ". Exception: " + e);
			throw new ArchiviaDocumentoException(e.getMessage());
		}
		finally
		{
			//			if(containsError)
			//			{
			//				if(cnmTRichiestaPk != null)
			//					getStatoRichiestaService().changeStatoRichiesta(cnmTRichiestaPk.getIdRichiesta(), STATO_RICHIESTA_ERRATA);
			//				else
			//				{
			//					log.debug(method + ". essuna richiesta inserita: non e possibile aggiornare la richiesta");
			//				}
			//			}
			log.debug(method + ". END");
		}
	}



	@SuppressWarnings("unused")
	public ResponseArchiviaDocumento archiviaDocumentoFisico(RequestArchiviaDocumentoFisico request) throws ArchiviaDocumentoException  {
		String method = "archiviaDocumentoFisico";
		log.debug(method + ". BEGIN");
		ResponseArchiviaDocumento response =  new ResponseArchiviaDocumento();
		boolean containsError = false;
		try{

			if(log.isDebugEnabled()){	
				log.debug(method + ". codice fruitore  = " + request.getCodiceFruitore());
				log.debug(method + ". folder           = " + request.getFolder());
				log.debug(method + ". tipo Documento   = " + request.getTipoDocumento());
			}

			CnmTRichiesta cnmTRichiestaN = null;
			
			if(StringUtils.isBlank(request.getDocumento().getNomeFile()) || request.getDocumento().getFile() == null) {
				RequestRicercaAllegato requestRicercaAllegato = new RequestRicercaAllegato();
				requestRicercaAllegato.setIdDocumento(request.getDocumento().getIdDocumento());
				requestRicercaAllegato.setCodiceFruitore(request.getCodiceFruitore());
				
				String idDocumento = request.getDocumento().getIdDocumento();
				int num = request.getDocumento().getNumeroAllegati();
				request.setDocumento(manageDocumentoHelper.ricercaAllegato(requestRicercaAllegato).getDocumento());	
				request.getDocumento().setIdDocumento(idDocumento);
				request.getDocumento().setNumeroAllegati(num);
			}

			// validazioni 	
			if(request == null) throw new ArchiviaDocumentoException("Request non valorizzata");
			if(request.getDocumento() == null) throw new ArchiviaDocumentoException("Documento non valorizzato");
			if(request.getDocumento().getFile() == null) throw new ArchiviaDocumentoException("File documento non valorizzato");
			if(request.getDocumento().getNomeFile() == null) throw new ArchiviaDocumentoException("Nome file documento non valorizzato");
			if(StringUtils.isBlank(request.getMimeType())) throw new ArchiviaDocumentoException("MimeType non valorizzato");
			
			
			if(request.getSoggetto() == null) throw new ProtocollaDocumentoException("Soggetto non valorizzato");
			if(request.getSoggetto().getTipologia() == null) throw new ProtocollaDocumentoException("Tipologia soggetto non valorizzato");
			if(request.getSoggetto().getCognome() == null) throw new ProtocollaDocumentoException("Cognome soggetto non valorizzato");
			if(request.getSoggetto().getNome() == null) throw new ProtocollaDocumentoException("Nome soggetto non valorizzato");
			
			if(StringUtils.isBlank(request.getCodiceFruitore())) throw new ArchiviaDocumentoException("Codice fruitore non presente");
			if(StringUtils.isBlank(request.getTipoDocumento())) throw new ArchiviaDocumentoException("Tipo documento non presente");
			if(StringUtils.isBlank(request.getFolder())) throw new ArchiviaDocumentoException("Folder non presente");


			// recupero fruitore
			CnmTFruitore cnmTFruitore = getFruitore();
			// tipo documento
			CnmDTipoDocumento cnmDTipoDocumento = getTipoDocumento(request.getTipoDocumento());
			//tipo fornitore
			CnmDTipoFornitore cnmDTipoFornitore = getCnmDTipoFornitoreRepository().findOne(DoquiConstants.FORNITORE_ACTA);

			// RICHIESTA
			CnmTRichiesta cnmTRichiesta = createCnmTRichiesta(cnmTFruitore.getIdFruitore(), DoquiConstants.SERVIZIO_INSERIMENTO_ARCHIVIAZIONE);


			// DOCUMENTO
			
			// 20200711_LC 
			CnmTUser cnmTUser = null;
			if (SecurityUtils.getAuthentication()==null) {
				cnmTUser = cnmTUserRepository.findByCodiceFiscaleAndFineValidita(DoquiConstants.USER_SCHEDULED_TASK);
			} else {
				UserDetails user = SecurityUtils.getUser();
				cnmTUser = cnmTUserRepository.findOne(user.getIdUser());
			}
			
			CnmTDocumento cnmTDocumento = new CnmTDocumento();
//			cnmTDocumento.setIdDocumento(getDocTDocumentoDaoIncrementer().nextLongValue());
			cnmTDocumento.setFolder(request.getFolder());
			cnmTDocumento.setCnmDTipoDocumento(cnmDTipoDocumento);	// 20200630_LC
			cnmTDocumento.setDataOraInsert(utilsDate.asTimeStamp(LocalDateTime.now()));	// 20200630_LC
			cnmTDocumento.setCnmTUser2(cnmTUser);		// 20200702_LC 
			
			if(request.getMetadati() != null){
//				cnmTDocumento.setCodiceFiscale(request.getMetadati().getCodiceFiscale());
				cnmTDocumento.setIdentificativoEntitaFruitore(request.getMetadati().getIdEntitaFruitore());
//				cnmTDocumento.setTarga(request.getMetadati().getTarga());
			}
			
			// 20201006_LC save anticipato
			cnmTDocumento = getCnmTDocumentoRepository().save(cnmTDocumento);
			cnmTDocumento.setIdentificativoArchiviazione(componiParolaChiave(String.valueOf(cnmTDocumento.getIdDocumento())));	// 20200713_LC
			CnmTDocumento cnmTDocumentoN =getCnmTDocumentoRepository().save(cnmTDocumento);	
			cnmTRichiestaN = getStatoRichiestaService().insertRichiesta(cnmTRichiesta);

			//RICHIESTA DOCUMENTO

			// 20200610_LC
			CnmRRichiestaDocumentoPK cnmRRichiestaDocumentoPK = new CnmRRichiestaDocumentoPK();
			cnmRRichiestaDocumentoPK.setIdDocumento(cnmTDocumento.getIdDocumento());
			cnmRRichiestaDocumentoPK.setIdRichiesta(cnmTRichiesta.getIdRichiesta());
			
			CnmRRichiestaDocumento cnmRRichiestaDocumentoN = new CnmRRichiestaDocumento();
			cnmRRichiestaDocumentoN.setId(cnmRRichiestaDocumentoPK);
			cnmRRichiestaDocumentoN.setCnmTDocumento(cnmTDocumento);
			cnmRRichiestaDocumentoN.setCnmTRichiesta(cnmTRichiesta);

			
			// 20200731_LC nuovo metadato collocazioneCartacea
			request.setCollocazioneCartacea(cnmDTipoDocumento.getCollocazioneCartacea());
			
			
			

			//POJO
			UtenteActa utenteActa = new UtenteActa();
			utenteActa.setCodiceFiscale(cnmTFruitore.getCfActa());
			utenteActa.setIdAoo(new Integer(cnmTFruitore.getIdAooActa()));
			utenteActa.setIdNodo(new Integer(cnmTFruitore.getIdNodoActa()));
			utenteActa.setIdStruttura(new Integer(cnmTFruitore.getIdStrutturaActa()));
			utenteActa.setApplicationKeyActa(cnmDTipoFornitore.getApplicationKey());
			utenteActa.setRepositoryName(cnmDTipoFornitore.getRepository());
			
			
			if(StringUtils.isNotBlank(request.getRootFolder()))
				utenteActa.setRootActa(request.getRootFolder());
			else
				utenteActa.setRootActa(cnmDTipoDocumento.getRoot());
			
			utenteActa.setIdvitalrecordcodetype(cnmDTipoDocumento.getIdVitalRecordCodeType());
			utenteActa.setIdStatoDiEfficacia(cnmDTipoDocumento.getIdStatoEfficacia());
			
			/*
			if(StringUtils.isNotEmpty(rootActa))
				 utenteActa.setRootActa(rootActa);
			else
				 utenteActa.setRootActa(cnmDTipoDocumentoDto.getRoot());
			*/
			
			
			//POJO
			DocumentoElettronicoActa documentoActa = new DocumentoElettronicoActa();
			documentoActa.setIdDocumento(cnmTDocumento.getIdentificativoArchiviazione());
			documentoActa.setFolder(request.getFolder());
			documentoActa.setFruitore(cnmTFruitore.getDescrFruitore());
			documentoActa.setApplicativoAlimentante(cnmTFruitore.getDescrFruitore());

			//documentoActa.setTipoStrutturaRoot(cnmDTipoDocumento.getIdStrutturaActaRoot());
			//documentoActa.setTipoStrutturaFolder(cnmDTipoDocumento.getIdStrutturaActaFolder());
			documentoActa.setTipoStrutturaRoot(cnmDTipoDocumento.getCnmDStrutturaActaRoot().getIdStrutturaActa());		// 20200630_LC
			documentoActa.setTipoStrutturaFolder(cnmDTipoDocumento.getCnmDStrutturaActaFolder().getIdStrutturaActa());	// 20200630_LC
			
			documentoActa.setStream(request.getDocumento().getFile());
			documentoActa.setNomeFile(request.getDocumento().getNomeFile());
			documentoActa.setDescrizione(cnmDTipoDocumento.getDescrTipoDocumento());
			documentoActa.setMimeType(request.getMimeType());
			documentoActa.setNumeroAllegati(request.getDocumento().getNumeroAllegati());
					
			
			if(request.getMetadati() != null) {
				MetadatiActa metadatiActa = new MetadatiActa();
				metadatiActa.setCodiceFiscale(request.getMetadati().getCodiceFiscale());
				metadatiActa.setIdEntitaFruitore(request.getMetadati().getIdEntitaFruitore());
				metadatiActa.setTarga(request.getMetadati().getTarga());
				documentoActa.setMetadatiActa(metadatiActa);
			}

			SoggettoActa soggettoActa = new SoggettoActa();
			if (Soggetto.TOPOLOGIA_SOGGETTO_MITTENTE.equals(request.getSoggetto().getTipologia())){
				soggettoActa.setMittente(true);
				documentoActa.setAutore(request.getSoggetto().getCognome());	
				utenteActa.setDescFormaDocumentaria(cnmDTipoDocumento.getDescFormadocEntrata());
			}
			else{
				soggettoActa.setMittente(false);
				documentoActa.setAutore("");
				utenteActa.setDescFormaDocumentaria(cnmDTipoDocumento.getDescFormadocUscita());
			}
			soggettoActa.setCognome(request.getSoggetto().getCognome());
			soggettoActa.setNome(request.getSoggetto().getNome());
			if (request.getSoggetto().getDenominazione() != null)
				soggettoActa.setDenominazione(request.getSoggetto().getDenominazione());

			documentoActa.setSoggettoActa(soggettoActa);
			
			documentoActa.setAutoreFisico(request.getAutoreFisico());
			documentoActa.setApplicativoAlimentante(request.getApplicativoAlimentante());
			

			documentoActa.setAutoreGiuridico(request.getAutoreGiuridico());
			documentoActa.setDestinatarioFisico(request.getDestinatarioFisico());
			documentoActa.setDestinatarioGiuridico(request.getDestinatarioGiuridico());
			documentoActa.setOriginatore(request.getOriginatore());
			
			// 20200731_LC
			documentoActa.setCollocazioneCartacea(request.getCollocazioneCartacea());
		
			if(log.isDebugEnabled())
			{
				log.debug(method + ". UtenteActa =\n " + XmlSerializer.objectToXml(utenteActa));
				log.debug(method + ". DocumentoActa =\n " + XmlSerializer.objectToXml(documentoActa));
			}

			// 20201006_LC save anticipato
			// CnmTDocumento cnmTDocumentoN =getCnmTDocumentoRepository().save(cnmTDocumento);
			// cnmTRichiestaN = getStatoRichiestaService().insertRichiesta(cnmTRichiesta);
			CnmRRichiestaDocumento cnmRRichiestaDocumento = getCnmRRichiestaDocumentoRepository().save(cnmRRichiestaDocumentoN);
			if(log.isDebugEnabled()){
				log.debug(method + ". inserito documento           = " + cnmTDocumentoN);
				log.debug(method + ". inserita richiesta           = " + cnmTRichiestaN);
				log.debug(method + ". inserita richiesta documento = " + cnmRRichiestaDocumento);

			}

			
			KeyDocumentoActa keyDocumentoActa = actaManagementService.archiviaDocumentoFisico(documentoActa, utenteActa);
			
			log.debug(method + ". KeyDocumentoActa =\n " + XmlSerializer.objectToXml(keyDocumentoActa));

			if (keyDocumentoActa != null){
				changeRiferimentiFornitoreDocumentoConKeyActa(cnmTDocumento.getIdDocumento(), keyDocumentoActa.getUUIDDocumento(), keyDocumentoActa.getNumeroProtocollo(),
					keyDocumentoActa.getClassificazioneId(), keyDocumentoActa.getRegistrazioneId(),  keyDocumentoActa.getObjectIdClassificazione(), keyDocumentoActa.getObjectIdDocumento());

				getStatoRichiestaService().changeStatoRichiesta(cnmTRichiesta.getIdRichiesta(), DoquiConstants.STATO_RICHIESTA_EVASA);

				//response.setIdDocumento(keyDocumentoActa.getParolaChiave());	
				response.setIdDocumento(String.valueOf(cnmTDocumento.getIdDocumento()));	// 20200713_LC
				response.setObjectIdDocumento(String.valueOf(cnmTDocumento.getObjectiddocumento()));	// 20201123_LC
			}
			else
				containsError = true;

			

			return response;

		}
		catch (FruitoreException e){
			containsError = true;
			log.error(method + ". FruitoreException: " + e);
			throw new ArchiviaDocumentoException(e.getMessage());
		}
		/*
			catch (IntegrationException e) {
				containsError = true;
				log.error(method + ". IntegrationException: " + e);
				throw new ArchiviaDocumentoException(e.getMessage());
			}
		 */
		catch (Exception e) {
			containsError = true;
			log.error(method + ". Exception: " + e);
			throw new ArchiviaDocumentoException(e.getMessage());
		}
		finally {

			log.debug(method + ". END");
		}
	}

}
