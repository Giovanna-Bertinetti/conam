import { AzioneVO } from "../../../commons/vo/select-vo";

export class AzioneVerbaleResponse{

    public azione: AzioneVO;
    public aggiungiAllegatoEnable: boolean;
    public modificaVerbaleEnable: boolean;
    public eliminaAllegatoEnable: boolean;

    constructor(){
        if(!this.azione) this.azione = new AzioneVO();
    }
}