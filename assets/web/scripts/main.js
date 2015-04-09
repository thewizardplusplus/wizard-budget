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
		function LoadActiveSpending() {
			var json = activity.getSetting('active_spending');
			SaveActiveSpending(null);

			return JSON.parse(json);
		}
		function SaveActiveSpending(active_spending) {
			var json = JSON.stringify(active_spending);
			activity.setSetting('active_spending', json);
		}
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
			var spendings = JSON.parse(raw_spendings);
			spendings.map(
				function(spending) {
					spending_list.append(
						'<li class = "table-view-cell media">'
							+ '<button '
								+ 'class = "btn second-list-button '
									+ 'edit-spending-button"'
								+ 'data-spending-id = "' + spending.id + '"'
								+ 'data-income = "'
									+ (spending.amount < 0
										? 'true'
										: 'false') + '"'
								+ 'data-timestamp = "'
									+ spending.timestamp + '">'
								+ '<i class = "fa fa-pencil"></i>'
							+ '</button>'
							+ '<button '
								+ 'class = "btn remove-spending-button"'
								+ 'data-spending-id = "' + spending.id + '">'
								+ '<i class = "fa fa-trash"></i>'
							+ '</button>'
							+ '<span '
								+ 'class = "'
									+ 'media-object '
									+ 'pull-left '
									+ 'mark-container'
								+ '">'
								+ (spending.has_credit_card_tag
									? '<i class = "fa fa-credit-card mark"></i>'
									: '')
								+ '<i class = "fa fa-'
									+ (spending.amount > 0
										? 'shopping-cart'
										: 'money')
									+ ' fa-2x"></i>'
							+ '</span>'
							+ '<div class = "media-body">'
								+ '<p>'
									+ '<span class = "underline">'
										+ '<strong>'
											+ '<span class = "date-view">'
												+ spending.date
											+ '</span>'
										+ '</strong> '
										+ '<span class = "time-view">'
											+ spending.time
										+ '</span>:'
									+ '</span>'
								+ '</p>'
								+ '<p>'
									+ '<span class = "amount-view">'
										+ Math.abs(spending.amount)
									+ '</span> '
									+ '<i class = "fa fa-ruble"></i>'
									+ (spending.comment.length
										? ' &mdash; '
											+ '<em>'
												+ '<span '
													+ 'class = "comment-view">'
													+ spending.comment
												+ '</span>'
											+ '</em>'
										: '')
								+ '.</p>'
							+ '</div>'
						+ '</li>'
					);
				}
			);

			var remove_dialog = $('#remove-dialog');
			$('.close-remove-dialog', remove_dialog).click(
				function() {
					remove_dialog.removeClass('active');
					SaveActiveSpending(null);

					return false;
				}
			);
			var remove_dialog_date_view = $('.date-view', remove_dialog);
			var remove_dialog_time_view = $('.time-view', remove_dialog);
			var remove_dialog_amount_view = $('.amount-view', remove_dialog);
			var remove_dialog_comment_view = $('.comment-view', remove_dialog);
			$('.remove-spending-button', remove_dialog).click(
				function() {
					var active_spending = LoadActiveSpending();
					if ($.type(active_spending) !== "null") {
						spending_manager.deleteSpending(active_spending.id);
						activity.updateWidget();

						PUSH({url: 'history.html'});
					}
				}
			);

			$('.edit-spending-button', spending_list).click(
				function() {
					var button = $(this);

					active_spending = {};
					active_spending.id = parseInt(button.data('spending-id'));
					active_spending.income_flag =
						button.data('income')
							? true
							: null;

					var timestamp = moment(
						parseInt(button.data('timestamp')) * 1000
					);
					active_spending.date = timestamp.format('YYYY-MM-DD');
					active_spending.time = timestamp.format('HH:mm');

					var list_item = button.parent();
					active_spending.amount =
						$('.amount-view', list_item)
						.text();
					active_spending.comment =
						$('.comment-view', list_item)
						.text();

					SaveActiveSpending(active_spending);
					PUSH({url: 'editor.html'});
				}
			);
			$('.remove-spending-button', spending_list).click(
				function() {
					var button = $(this);

					active_spending = {};
					active_spending.id = parseInt(button.data('spending-id'));
					SaveActiveSpending(active_spending);

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
					GUI.hideMainMenu();

					var filename = spending_manager.backup();
					if (
						activity.getSetting('save_backup_to_dropbox') == "true"
						&& filename.length
					) {
						activity.saveToDropbox(filename);
					}
				}
			);
			$('.restore-button').click(
				function() {
					GUI.hideMainMenu();
					activity.selectBackupForRestore();
				}
			);
			$('.settings-button').click(
				function() {
					GUI.hideMainMenu();
					activity.openSettings();
				}
			);
		}
		function UpdateIndexPage() {
			UpdateControlButtons();
			UpdateSpendingList();
		}
		function UpdateEditorPage() {
			var active_spending = LoadActiveSpending();

			var edit_spending_button = $('form .edit-spending-button');
			if ($.type(active_spending) === "null") {
				$('.title').text('Add');
				$('.button-icon', edit_spending_button)
					.removeClass('fa-save')
					.addClass('fa-plus');
				$('.button-text', edit_spending_button).text('Add');
			} else {
				$('.title').text('Edit');
				$('.button-icon', edit_spending_button)
					.removeClass('fa-plus')
					.addClass('fa-save');
				$('.button-text', edit_spending_button).text('Save');
			}

			var date_editor = $('.date-editor');
			if ($.type(active_spending) !== "null") {
				date_editor.val(active_spending.date);
				date_editor.show();
			}

			var time_editor = $('.time-editor');
			if ($.type(active_spending) !== "null") {
				time_editor.val(active_spending.time);
				time_editor.show();
			}

			if ($.type(active_spending) !== "null") {
				$('hr').show();
			}

			var amount_editor = $('.amount-editor');
			if ($.type(active_spending) !== "null") {
				amount_editor.val(active_spending.amount);
			}
			amount_editor.focus();

			var comment_editor = $('.comment-editor');
			if ($.type(active_spending) !== "null") {
				comment_editor.val(active_spending.comment);
			}

			var raw_tags = spending_manager.getSpendingTags();
			var tags = JSON.parse(raw_tags);
			var default_tags =
				$.type(active_spending) !== "null"
					? active_spending.comment.split(',')
					: [];
			var tags_editor = new WizardTags(
				'.tags-editor',
				{
					tags: tags,
					default_tags: default_tags,
					separators: ',',
					only_unique: true,
					placeholder: 'Tags'
				}
			);

			var income_flag = $('.income-flag');
			if ($.type(active_spending) !== "null") {
				if (active_spending.income_flag) {
					income_flag.addClass('active');
				}
			}

			edit_spending_button.click(
				function() {
					var amount = Math.abs(parseFloat(amount_editor.val()));
					var comment = tags_editor.getTags().join(', ');
					if (income_flag.hasClass('active')) {
						amount *= -1;
					}

					if ($.type(active_spending) === "null") {
						spending_manager.createSpending(amount, comment);
					} else {
						var date = date_editor.val();
						var time = time_editor.val();
						spending_manager.updateSpending(
							active_spending.id,
							date,
							time,
							amount,
							comment
						);
					}

					activity.updateWidget();
					PUSH({url: 'history.html'});

					return false;
				}
			);
		}
		function UpdateSmsPage() {
			var sms_list = $('.sms-list');
			sms_list.empty();

			var raw_spendings = spending_manager.getSpendingsFromSms();
			var spendings = JSON.parse(raw_spendings);
			spendings.map(
				function(spending) {
					sms_list.append(
						'<li '
							+ 'class = "table-view-cell media" '
							+ 'data-timestamp = "'
								+ spending.timestamp
							+ '" '
							+ 'data-amount = "' + spending.amount + '">'
							+ '<div class = "toggle active import-flag">'
								+ '<div class = "toggle-handle"></div>'
							+ '</div>'
							+ '<span class = "'
								+ 'media-object '
								+ 'pull-left '
								+ 'mark-container'
							+ '">'
								+ '<i class = "fa fa-credit-card mark"></i>'
								+ '<i class = "fa fa-'
									+ (spending.amount > 0
										? 'shopping-cart'
										: 'money')
									+ ' fa-2x"></i>'
							+ '</span>'
							+ '<div class = "media-body">'
								+ '<p>'
									+ '<span class = "underline">'
										+ '<strong>'
											+ spending.date
										+ '</strong>'
										+ ' ' + spending.time + ':'
									+ '</span>'
								+ '</p>'
								+ '<p>'
									+ Math.abs(spending.amount)
									+ '<i class = "fa fa-ruble"></i>.'
								+ '</p>'
							+ '</div>'
						+ '</li>'
					);
				}
			);

			$('.import-sms-button').click(
				function() {
					var sms_data = [];
					$('li', sms_list).each(
						function() {
							var list_item = $(this);
							if ($('.import-flag.active', list_item).length) {
								sms_data.push(
									{
										timestamp: list_item.data('timestamp'),
										amount: list_item.data('amount')
									}
								);
							}
						}
					);

					var sms_data_in_string = JSON.stringify(sms_data);
					spending_manager.importSms(sms_data_in_string);

					activity.updateWidget();
					PUSH({url: 'history.html'});
				}
			);
		}

		window.addEventListener(
			'push',
			function(event) {
				if (/\bhistory\b/.test(event.detail.state.url)) {
					activity.setSetting('current_page', 'history');
					UpdateIndexPage();
				} else if (/\beditor\b/.test(event.detail.state.url)) {
					activity.setSetting('current_page', 'editor');
					UpdateEditorPage();
				} else if (/\bsms\b/.test(event.detail.state.url)) {
					activity.setSetting('current_page', 'sms');
					UpdateSmsPage();
				} else if (/\bauthors\b/.test(event.detail.state.url)) {
					activity.setSetting('current_page', 'authors');
				} else {
					activity.setSetting('current_page', 'history');
				}
			}
		);
		PUSH({url: activity.getSetting('current_page') + '.html'});
	}
);
