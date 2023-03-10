package com.d.me.views.patients;

import com.d.me.data.entity.SystemUser;
import com.d.me.data.service.SystemUserService;
import com.d.me.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.RolesAllowed;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@PageTitle("Patients")
@Route(value = "Patients/:systemUserID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class PatientsView extends Div implements BeforeEnterObserver {

    private final String SYSTEMUSER_ID = "systemUserID";
    private final String SYSTEMUSER_EDIT_ROUTE_TEMPLATE = "Patients/%s/edit";

    private final Grid<SystemUser> grid = new Grid<>(SystemUser.class, false);

    private TextField userID;
    private TextField username;
    private TextField password;
    private TextField firstname;
    private TextField surname;
    private DatePicker dob;
    private TextField sex;
    private TextField address;
    private TextField phone;
    private TextField email;
    private TextField role;
    private TextField notes;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<SystemUser> binder;

    private SystemUser systemUser;

    private final SystemUserService systemUserService;

    public PatientsView(SystemUserService systemUserService) {
        this.systemUserService = systemUserService;
        addClassNames("patients-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("userID").setAutoWidth(true);
        grid.addColumn("username").setAutoWidth(true);
        grid.addColumn("password").setAutoWidth(true);
        grid.addColumn("firstname").setAutoWidth(true);
        grid.addColumn("surname").setAutoWidth(true);
        grid.addColumn("dob").setAutoWidth(true);
        grid.addColumn("sex").setAutoWidth(true);
        grid.addColumn("address").setAutoWidth(true);
        grid.addColumn("phone").setAutoWidth(true);
        grid.addColumn("email").setAutoWidth(true);
        grid.addColumn("role").setAutoWidth(true);
        grid.addColumn("notes").setAutoWidth(true);
        grid.setItems(query -> systemUserService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(SYSTEMUSER_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(PatientsView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(SystemUser.class);

        // Bind fields. This is where you'd define e.g. validation rules
        binder.forField(userID).withConverter(new StringToIntegerConverter("Only numbers are allowed")).bind("userID");
        binder.forField(sex).withConverter(new StringToIntegerConverter("Only numbers are allowed")).bind("sex");

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.systemUser == null) {
                    this.systemUser = new SystemUser();
                }
                binder.writeBean(this.systemUser);
                systemUserService.update(this.systemUser);
                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                UI.getCurrent().navigate(PatientsView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> systemUserId = event.getRouteParameters().get(SYSTEMUSER_ID).map(Long::parseLong);
        if (systemUserId.isPresent()) {
            Optional<SystemUser> systemUserFromBackend = systemUserService.get(systemUserId.get());
            if (systemUserFromBackend.isPresent()) {
                populateForm(systemUserFromBackend.get());
            } else {
                Notification.show(String.format("The requested systemUser was not found, ID = %s", systemUserId.get()),
                        3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(PatientsView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        userID = new TextField("User ID");
        username = new TextField("Username");
        password = new TextField("Password");
        firstname = new TextField("Firstname");
        surname = new TextField("Surname");
        dob = new DatePicker("Dob");
        sex = new TextField("Sex");
        address = new TextField("Address");
        phone = new TextField("Phone");
        email = new TextField("Email");
        role = new TextField("Role");
        notes = new TextField("Notes");
        formLayout.add(userID, username, password, firstname, surname, dob, sex, address, phone, email, role, notes);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(SystemUser value) {
        this.systemUser = value;
        binder.readBean(this.systemUser);

    }
}
