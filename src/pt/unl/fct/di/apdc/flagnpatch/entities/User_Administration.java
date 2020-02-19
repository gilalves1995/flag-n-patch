package pt.unl.fct.di.apdc.flagnpatch.entities;

import pt.unl.fct.di.apdc.flagnpatch.inputData.AddressData;

import java.io.Serializable;

public class User_Administration extends User_Global implements Serializable {
	public static final String PROPERTY_INTERNAL_ID = "internalId";

	public String internalId;

	public User_Administration() {
		super();
	}

	public User_Administration(String name, String email, AddressData address, String role, String internalId) {
		super(name, email, address, role);
		this.internalId = internalId;
	}

	public User_Administration(String name, String email, AddressData address, 
				String role, String internalId, boolean isAccountBlocked) {
		super(name, email, address, role);
		this.internalId = internalId;
		this.isAccountBlocked = isAccountBlocked;
	}
}
