package pt.unl.fct.di.apdc.flagnpatch.inputData;


// TODO: VERIFICAR USO DA CLASSE

public class ReportTypeData {

    public String name;
    public String responsible;

    public ReportTypeData() {
    }

    public ReportTypeData(String name, String responsible) {
        this.name = name;
        this.responsible = responsible;
    }

    // For now, only the type name is being verified, later more fields will be added to this class
    public boolean validTypeData() {
        return validField(name);
    }

    public boolean hasResponsible() {
        return validField(responsible);
    }

    private boolean validField(String value) {
        return value != null && !value.equals("");
    }


}
