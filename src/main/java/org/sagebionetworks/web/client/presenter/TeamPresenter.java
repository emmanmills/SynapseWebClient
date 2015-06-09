package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.TeamView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.team.InviteWidget;
import org.sagebionetworks.web.client.widget.team.JoinTeamWidget;
import org.sagebionetworks.web.client.widget.team.MemberListWidget;
import org.sagebionetworks.web.client.widget.team.OpenMembershipRequestsWidget;
import org.sagebionetworks.web.client.widget.team.OpenUserInvitationsWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamDeleteModalWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamEditModalWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamLeaveModalWidget;
import org.sagebionetworks.web.shared.TeamBundle;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamPresenter extends AbstractActivity implements TeamView.Presenter, Presenter<org.sagebionetworks.web.client.place.Team> {
		
	private org.sagebionetworks.web.client.place.Team place;
	private TeamView view;
	private SynapseClientAsync synapseClient;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private JSONObjectAdapter jsonObjectAdapter;
	private Team team;
	private SynapseAlert synAlert;
	private TeamLeaveModalWidget leaveTeamWidget;
	private TeamDeleteModalWidget deleteTeamWidget;
	private TeamEditModalWidget editTeamWidget;
	private InviteWidget inviteWidget;
	private JoinTeamWidget joinTeamWidget;
	private MemberListWidget memberListWidget;
	private OpenMembershipRequestsWidget openMembershipRequestsWidget;
	private OpenUserInvitationsWidget openUserInvitationsWidget;
	
	@Inject
	public TeamPresenter(TeamView view,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			JSONObjectAdapter jsonObjectAdapter,
			SynapseAlert synAlert, TeamLeaveModalWidget leaveTeamWidget,
			TeamDeleteModalWidget deleteTeamWidget,
			TeamEditModalWidget editTeamWidget, InviteWidget inviteWidget,
			JoinTeamWidget joinTeamWidget,  
			MemberListWidget memberListWidget, 
			OpenMembershipRequestsWidget openMembershipRequestsWidget,
			OpenUserInvitationsWidget openUserInvitationsWidget) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.synAlert = synAlert;
		this.leaveTeamWidget = leaveTeamWidget;
		this.deleteTeamWidget = deleteTeamWidget;
		this.editTeamWidget = editTeamWidget;
		this.inviteWidget = inviteWidget;
		this.joinTeamWidget = joinTeamWidget;
		this.memberListWidget = memberListWidget;
		this.openMembershipRequestsWidget = openMembershipRequestsWidget;
		this.openUserInvitationsWidget = openUserInvitationsWidget;
		view.setPresenter(this);
		view.setSynAlertWidget(synAlert.asWidget());
		view.setLeaveTeamWidget(leaveTeamWidget.asWidget());
		view.setDeleteTeamWidget(deleteTeamWidget.asWidget());
		view.setEditTeamWidget(editTeamWidget.asWidget());
		view.setInviteMemberWidget(inviteWidget.asWidget());
		view.setJoinTeamWidget(joinTeamWidget.asWidget());
		view.setOpenMembershipRequestWidget(memberListWidget.asWidget());
		view.setOpenUserInvitationsWidget(openMembershipRequestsWidget.asWidget());
		view.setMemberListWidget(openUserInvitationsWidget.asWidget());
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(org.sagebionetworks.web.client.place.Team place) {
		this.place = place;
		this.view.setPresenter(this);
		this.view.clear();
		showView(place);
	}
	
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
	

	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	private void refresh() {
		refresh(team.getId());
	}
	
	@Override
	public void refresh(final String teamId) {
		synAlert.clear();
		synapseClient.getTeamBundle(authenticationController.getCurrentUserPrincipalId(), teamId, authenticationController.isLoggedIn(), new AsyncCallback<TeamBundle>() {
			@Override
			public void onSuccess(TeamBundle result) {
				view.clear();
				team = result.getTeam();
				TeamMembershipStatus teamMembershipStatus = result.getTeamMembershipStatus();
				boolean isAdmin = result.isUserAdmin();
				Callback refreshCallback = new Callback() {
					@Override
					public void invoke() {
						refresh(teamId);
					}
				};
				view.setPublicJoinVisible(team.getCanPublicJoin());
				view.setTotalMemberCount(result.getTotalMemberCount().toString());
				view.setMediaObjectPanel(team);			
				memberListWidget.configure(teamId, isAdmin, refreshCallback);				

				if (teamMembershipStatus != null) {
					if (!teamMembershipStatus.getIsMember())
						//not a member, add Join widget
						joinTeamWidget.configure(teamId, false, teamMembershipStatus,
								refreshCallback, null, null, null, null, false);
					else {
						view.showMemberMenuItems();
						if (isAdmin) {
							openMembershipRequestsWidget.configure(teamId, refreshCallback);
							openUserInvitationsWidget.configure(teamId, refreshCallback);
							view.showAdminMenuItems();
						}
					}
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
		
	private void showView(org.sagebionetworks.web.client.place.Team place) {
		String teamId = place.getTeamId();
		refresh(teamId);
	}

	@Override
	public void deleteTeam() {
		synAlert.clear();
		synapseClient.deleteTeam(team.getId(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				//go home
				view.showInfo(DisplayConstants.DELETE_TEAM_SUCCESS, "");
				globalApplicationState.gotoLastPlace();
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}

	@Override
	public void leaveTeam() {
		synAlert.clear();
		leaveTeamWidget.setRefreshCallback(new Callback() {
			@Override
			public void invoke() {
				refresh();
			}
		});
		leaveTeamWidget.setTeam(team);
		leaveTeamWidget.showDialog();
	}

	@Override
	public void updateTeamInfo(String name, String description, boolean canPublicJoin, String fileHandleId) {
		synAlert.clear();
		if (name == null || name.trim().length() == 0) {
			//syn alert handles?!
			view.showErrorMessage(DisplayConstants.ERROR_NAME_MUST_BE_DEFINED);
		}
		else {
			team.setName(name);
			team.setDescription(description);
			team.setCanPublicJoin(canPublicJoin);
			team.setIcon(fileHandleId);
			synapseClient.updateTeam(team, new AsyncCallback<Team>() {
				@Override
				public void onSuccess(Team result) {
					view.showInfo(DisplayConstants.UPDATE_TEAM_SUCCESS, "");
					refresh();
				}
				@Override
				public void onFailure(Throwable caught) {
					synAlert.handleException(caught);
				}
			});
		}
	}
	
	@Override
	public void showInviteModal() {
		synAlert.clear();
		inviteWidget.setRefreshCallback(new Callback() {
			@Override
			public void invoke() {
				refresh();
			}
		});
		inviteWidget.setTeam(team);
		inviteWidget.setVisible(true);
	}

	@Override
	public void showEditModal() {
		synAlert.clear();
		editTeamWidget.setRefreshCallback(new Callback() {
			@Override
			public void invoke() {
				refresh();
			}
		});
		editTeamWidget.setTeam(team);
		editTeamWidget.setVisible(true);
	}

	@Override
	public void showDeleteModal() {
		synAlert.clear();
		deleteTeamWidget.setRefreshCallback(new Callback() {
			@Override
			public void invoke() {
				refresh();
			}
		});
		deleteTeamWidget.setTeam(team);
		deleteTeamWidget.showDialog();
	}

	@Override
	public void showLeaveModal() {
		synAlert.clear();
		leaveTeamWidget.setRefreshCallback(new Callback() {
			@Override
			public void invoke() {
				refresh();
			}
		});
		leaveTeamWidget.setTeam(team);
		leaveTeamWidget.showDialog();		
	}
	
	//testing only
	public void setTeam(Team team) {
		this.team = team;
	}
}

