package pt.unl.fct.di.apdc.flagnpatch.utils;

public enum TypeEnum {

    ROAD_SAFETY("Estradas e Sinalização"),
    HYGIENE("Higiene Urbana e Animais"),
    LIGHTNING("Iluminação Pública"),
    SANITATION("Saneamento"),
    GREEN_SPACES("Árvores e Espaços Verdes");
    private final String typeDescription;

    TypeEnum(String typeDescription) {
        this.typeDescription = typeDescription;
    }

    public String getTypeDescription() {
        return this.typeDescription;
    }

}
