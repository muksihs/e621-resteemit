package muksihs.e621.resteemit.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialInput;
import gwt.material.design.client.ui.MaterialModal;
import muksihs.e621.resteemit.client.Event;

public class LoginUi extends Composite {

	@UiField
	protected MaterialButton btnCancel;
	@UiField
	protected MaterialInput username;
	@UiField
	protected MaterialInput wif;
	@UiField
	protected MaterialButton btnLogin;
	@UiField
	protected MaterialModal modal;

	private static LoginModalUiBinder uiBinder = GWT.create(LoginModalUiBinder.class);

	interface LoginModalUiBinder extends UiBinder<Widget, LoginUi> {
	}

	public LoginUi() {
		initWidget(uiBinder.createAndBindUi(this));
		setTitle("Login!");
		modal.addCloseHandler((e) -> removeFromParent());
		btnCancel.addClickHandler((e) -> modal.close());
		btnLogin.addClickHandler((e) -> {
			modal.close();
			fireEvent(new Event.Loading(true));
			fireEvent(new Event.TryLogin(username.getValue(), wif.getValue()));
		});
	}

	public void open() {
		modal.open();
	}

}
