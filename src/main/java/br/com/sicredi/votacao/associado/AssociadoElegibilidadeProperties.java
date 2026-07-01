package br.com.sicredi.votacao.associado;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "votacao.elegibilidade")
public class AssociadoElegibilidadeProperties {

    private boolean randomEnabled = true;
    private List<String> cpfsInvalidos = new ArrayList<>();
    private List<String> cpfsUnable = new ArrayList<>();
    private List<String> cpfsAble = new ArrayList<>();

    public boolean isRandomEnabled() {
        return randomEnabled;
    }

    public void setRandomEnabled(boolean randomEnabled) {
        this.randomEnabled = randomEnabled;
    }

    public List<String> getCpfsInvalidos() {
        return cpfsInvalidos;
    }

    public void setCpfsInvalidos(List<String> cpfsInvalidos) {
        this.cpfsInvalidos = cpfsInvalidos;
    }

    public List<String> getCpfsUnable() {
        return cpfsUnable;
    }

    public void setCpfsUnable(List<String> cpfsUnable) {
        this.cpfsUnable = cpfsUnable;
    }

    public List<String> getCpfsAble() {
        return cpfsAble;
    }

    public void setCpfsAble(List<String> cpfsAble) {
        this.cpfsAble = cpfsAble;
    }
}
