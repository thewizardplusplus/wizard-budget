var GUI = {
	hideMainMenu: function() {
		var event = new CustomEvent('touchend');
		$('.backdrop').get(0).dispatchEvent(event);
	},
	refresh: function() {
		activity.updateWidget();
		PUSH({url: 'history.html'});
	},
	back: function() {
		if (!/\bhistory\b/.test(window.location)) {
			PUSH({url: 'history.html'});
		} else {
			var remove_dialog = $('#remove-dialog');
			if (remove_dialog.hasClass('active')) {
				remove_dialog.removeClass('active');
			} else if ($('.popover').hasClass('visible')) {
				this.hideMainMenu();
			} else {
				activity.quit();
			}
		}
	}
};

$(document).ready(
	function() {
		var active_spending_id = null;
		var active_spending_date = null;
		var active_spending_time = null;
		var active_spending_amount = null;
		var active_spending_comment = null;
		var active_spending_income_flag = null;

		function UpdateSpendingList() {
			var spendings_sum_view = $('.spendings-sum-view');
			var spendings_sum = spending_manager.getSpendingsSum();
			spendings_sum_view.text(spendings_sum);
			if (spendings_sum <= 0) {
				spendings_sum_view.addClass('excess').removeClass('lack');
			} else {
				spendings_sum_view.addClass('lack').removeClass('excess');
			}

			var spending_list = $('.spending-list');
			spending_list.empty();

			var raw_spendings = spending_manager.getAllSpendings();
			var spendings = $.parseJSON(raw_spendings);
			spendings.map(
		        function(spending) {
					spending_list.append(
						'<li class = "table-view-cell media">'
							+ '<button class = "btn second-list-button edit-spending-button" data-spending-id = "' + spending.id + '" data-income = "' + (spending.amount < 0 ? 'true' : 'false') + '" data-timestamp = "' + spending.timestamp + '"><i class = "fa fa-pencil"></i></button>'
							+ '<button class = "btn remove-spending-button" data-spending-id = "' + spending.id + '"><i class = "fa fa-trash"></i></button>'
							+ '<span class = "media-object pull-left">'
							    + '<i class = "fa fa-'
							        + (spending.amount > 0
								        ? 'shopping-cart'
									    : 'money')
									+ ' fa-2x"></i>'
							    + '</span>'
							+ '<div class = "media-body">'
								+ '<p><span class = "underline"><strong><span class = "date-view">' + spending.date + '</span></strong> <span class = "time-view">' + spending.time + '</span>:</span></p>'
				                + '<p>'
							        + '<span class = "amount-view">' + Math.abs(spending.amount) + '</span> <i class = "fa fa-ruble"></i>'
								    + (spending.comment.length
								        ? ' &mdash; <em><span class = "comment-view">' + spending.comment + '</span></em>'
									    : '')
							    + '.</p>'
							+ '</div>'
						+ '</li>'
					);
				}
			);

			var remove_dialog = $('#remove-dialog');
			$('.remove-spending-button', remove_dialog).click(
				function() {
					if ($.type(active_spending_id) !== "null") {
						spending_manager.deleteSpending(active_spending_id);
						activity.updateWidget();
						active_spending_id = null;

						PUSH({url: 'history.html'});
					}
				}
			);
			var remove_dialog_date_view = $('.date-view', remove_dialog);
			var remove_dialog_time_view = $('.time-view', remove_dialog);
			var remove_dialog_amount_view = $('.amount-view', remove_dialog);
			var remove_dialog_comment_view = $('.comment-view', remove_dialog);

			$('.edit-spending-button', spending_list).click(
				function() {
					var button = $(this);
					active_spending_id = parseInt(button.data('spending-id'));
					active_spending_income_flag = button.data('income') ? true : null;

					var timestamp = moment(parseInt(button.data('timestamp')) * 1000);
					active_spending_date = timestamp.format('YYYY-MM-DD');
					active_spending_time = timestamp.format('hh:mm');

					var list_item = button.parent();
					active_spending_amount = $('.amount-view', list_item).text();
					active_spending_comment = $('.comment-view', list_item).text();

					PUSH({url: 'editor.html'});
				}
			);
			$('.remove-spending-button', spending_list).click(
				function() {
					var button = $(this);
					active_spending_id = parseInt(button.data('spending-id'));

					var list_item = button.parent();
					var date = $('.date-view', list_item).text();
					var time = $('.time-view', list_item).text();
					var amount = $('.amount-view', list_item).text();
					var comment = $('.comment-view', list_item).text();

					remove_dialog_date_view.text(date);
					remove_dialog_time_view.text(time);
					remove_dialog_amount_view.text(amount);
					if (comment.length) {
						remove_dialog_comment_view.html(' &mdash; ' + comment);
					}

					remove_dialog.addClass('active');
				}
			);
		}
		function UpdateControlButtons() {
		    $('.backup-button').click(
			    function() {
				    spending_manager.backup();
					GUI.hideMainMenu();
		        }
		    );
			$('.restore-button').click(
			    function() {
				    activity.selectBackupForRestore();
					GUI.hideMainMenu();
		        }
		    );
			$('.settings-button').click(
			    function() {
				    activity.openSettings();
					GUI.hideMainMenu();
		        }
		    );
		}
		function UpdateIndexPage() {
			UpdateControlButtons();
			UpdateSpendingList();
		}
		function UpdateEditorPage() {
			var spending_id = active_spending_id;
			active_spending_id = null;

			var edit_spending_button = $('header .edit-spending-button');
			if ($.type(spending_id) === "null") {
				$('.button-icon', edit_spending_button).removeClass('fa-save').addClass('fa-plus');
				$('.button-text', edit_spending_button).text('Add');
			} else {
				$('.button-icon', edit_spending_button).removeClass('fa-plus').addClass('fa-save');
				$('.button-text', edit_spending_button).text('Save');
			}

			var current_timestamp = moment();
			var date_editor = $('.date-editor');
			if ($.type(spending_id) !== "null") {
				date_editor.show();
				date_editor.val(active_spending_date);
			}

			var time_editor = $('.time-editor');
			if ($.type(spending_id) !== "null") {
				time_editor.show();
				time_editor.val(active_spending_time);
			}

			if ($.type(spending_id) !== "null") {
				$('hr').show();
			}

			var amount_editor = $('.amount-editor');
			if ($.type(active_spending_amount) !== "null") {
				amount_editor.val(active_spending_amount);
				active_spending_amount = null;
			}
			amount_editor.focus();

			var comment_editor = $('.comment-editor');
			if ($.type(active_spending_comment) !== "null") {
				comment_editor.val(active_spending_comment);
				active_spending_comment = null;
			}

			var income_flag = $('.income-flag');
			if ($.type(active_spending_income_flag) !== "null") {
				if (active_spending_income_flag) {
					income_flag.addClass('active');
				}
				active_spending_income_flag = null;
			}

			edit_spending_button.click(
		        function() {
			        var amount = Math.abs(parseFloat(amount_editor.val()));
					var comment = comment_editor.val();
					if (income_flag.hasClass('active')) {
						amount *= -1;
					}

					if ($.type(spending_id) === "null") {
				    	spending_manager.createSpending(amount, comment);
					} else {
						var date = date_editor.val();
						var time = time_editor.val();
						var timestamp = date + ' ' + time + ':00';

						spending_manager.updateSpending(spending_id, timestamp, amount, comment);
					}

					activity.updateWidget();
					PUSH({url: 'history.html'});
	            }
		    );
		}
		
		window.addEventListener(
		    'push',
		    function(event) {
				if (/\bhistory\b/.test(event.detail.state.url)) {
					UpdateIndexPage();
				} else if (/\beditor\b/.test(event.detail.state.url)) {
					UpdateEditorPage();
				}
			}
		);
		PUSH({url: 'history.html'});
	}
);

