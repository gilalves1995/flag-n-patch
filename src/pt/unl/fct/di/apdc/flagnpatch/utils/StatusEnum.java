package pt.unl.fct.di.apdc.flagnpatch.utils;

public enum StatusEnum {
    SUBMITED("Pendente"),
    IN_RESOLUTION("Em Resolução"),
    CLOSED("Resolvido"),
    REJECTED("Rejeitado"),
    ERROR("Com erros");

    private String statusDescription;

    StatusEnum(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

}
