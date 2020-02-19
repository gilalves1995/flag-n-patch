package pt.unl.fct.di.apdc.flagnpatch.utils;

public enum RolesEnum {

    TRIAL_USER("trial"),
    BASIC_USER("basic"),
    WORKER_USER("work"),
    CORE_USER("core"),
    
    END_USER("end"),
    
    
    IT_USER("it");

    private String roleDescription;

    RolesEnum(String roleDescription) {
        this.roleDescription = roleDescription;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

}
