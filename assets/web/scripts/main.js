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
			} else {
				remove_dialog = $('#remove-buy-dialog');
				if (remove_dialog.hasClass('active')) {
					remove_dialog.removeClass('active');
				} else {
					if ($('.popover').hasClass('visible')) {
						this.hideMainMenu();
					} else {
						activity.quit();
					}
				}
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
		function LoadActiveBuy() {
			var json = activity.getSetting('active_buy');
			SaveActiveBuy(null);

			return JSON.parse(json);
		}
		function SaveActiveBuy(active_buy) {
			var json = JSON.stringify(active_buy);
			activity.setSetting('active_buy', json);
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
		function UpdateBuyList() {
			var costs_sum_view = $('.costs-sum-view');
			var costs_sum = buy_manager.getCostsSum();
			costs_sum_view.text(costs_sum);

			var buy_list = $('.buy-list');
			buy_list.empty();

			var raw_buys = buy_manager.getAllBuys();
			var buys = JSON.parse(raw_buys);
			buys.map(
				function(buy) {
					buy_list.append(
						'<li '
							+ 'class = "'
								+ 'table-view-cell '
								+ 'media '
								+ (buy.status
									? 'buyed'
									: '')
							+ '">'
							+ '<button '
								+ 'class = "btn second-list-button '
									+ 'edit-buy-button"'
								+ 'data-buy-id = "' + buy.id + '"'
								+ 'data-status = "' + buy.status + '">'
								+ '<i class = "fa fa-pencil"></i>'
							+ '</button>'
							+ '<button '
								+ 'class = "btn remove-buy-button"'
								+ 'data-buy-id = "' + buy.id + '">'
								+ '<i class = "fa fa-trash"></i>'
							+ '</button>'
							+ '<span '
								+ 'class = "'
									+ 'media-object '
									+ 'pull-left'
								+ '">'
								+ '<i '
									+ 'class = "'
										+ 'fa '
										+ 'fa-' + (buy.status
											? 'gift'
											: 'shopping-cart') + ' '
										+ 'fa-2x'
									+ '">'
								+ '</i>'
							+ '</span>'
							+ '<div class = "media-body">'
								+ '<p>'
									+ '<span class = "underline">'
										+ '<strong>'
											+ '<span class = "name-view">'
												+ buy.name
											+ '</span>:'
										+ '</strong>'
									+ '</span>'
								+ '</p>'
								+ '<p>'
									+ '<span class = "cost-view">'
										+ buy.cost
									+ '</span> '
									+ '<i class = "fa fa-ruble"></i>.'
								+ '</p>'
							+ '</div>'
						+ '</li>'
					);
				}
			);

			var remove_dialog = $('#remove-buy-dialog');
			$('.close-remove-dialog', remove_dialog).click(
				function() {
					remove_dialog.removeClass('active');
					SaveActiveBuy(null);

					return false;
				}
			);
			var remove_dialog_name_view = $('.name-view', remove_dialog);
			var remove_dialog_cost_view = $('.cost-view', remove_dialog);
			$('.remove-buy-button', remove_dialog).click(
				function() {
					var active_buy = LoadActiveBuy();
					if ($.type(active_buy) !== "null") {
						buy_manager.deleteBuy(active_buy.id);
						activity.updateWidget();

						PUSH({url: 'history.html'});
					}
				}
			);

			$('.edit-buy-button', buy_list).click(
				function() {
					var button = $(this);

					active_buy = {};
					active_buy.id = parseInt(button.data('buy-id'));
					active_buy.status =
						button.data('status')
							? true
							: null;

					var list_item = button.parent();
					active_buy.name = $('.name-view', list_item).text();
					active_buy.cost = $('.cost-view', list_item).text();

					SaveActiveBuy(active_buy);
					PUSH({url: 'buy_editor.html'});
				}
			);
			$('.remove-buy-button', buy_list).click(
				function() {
					var button = $(this);

					active_buy = {};
					active_buy.id = parseInt(button.data('buy-id'));
					SaveActiveBuy(active_buy);

					var list_item = button.parent();
					var name = $('.name-view', list_item).text();
					var cost = $('.cost-view', list_item).text();

					remove_dialog_name_view.text(name);
					remove_dialog_cost_view.text(cost);
					remove_dialog.addClass('active');
				}
			);
		}
		function UpdateControlButtons() {
			$('.backup-button').click(
				function() {
					GUI.hideMainMenu();

					var filename = backup_manager.backup();
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

			$('.add-button').click(
				function() {
					if ($('#buys-segment').hasClass('active')) {
						PUSH({url: 'buy_editor.html'});
					} else {
						PUSH({url: 'editor.html'});
					}
				}
			);
		}
		function UpdateSegments() {
			$('.control-item, .control-content').removeClass('active');
			var current_segment = activity.getSetting('current_segment');
			$('.' + current_segment + '-segment').addClass('active');

			$('.control-item').on(
				'touchend',
				function() {
					var self = $(this);
					if (self.hasClass('buys-segment')) {
						activity.setSetting('current_segment', 'buys');
					} else {
						activity.setSetting('current_segment', 'history');
					}
				}
			);
		}
		function UpdateIndexPage() {
			UpdateControlButtons();
			UpdateSegments();
			UpdateSpendingList();
			UpdateBuyList();
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

			var raw_tags = spending_manager.getSpendingTags();
			var tags = JSON.parse(raw_tags);
			var raw_buy_names = buy_manager.getBuyNames();
			var buy_names = JSON.parse(raw_buy_names);
			tags = tags.concat(buy_names);

			var default_tags =
				$.type(active_spending) !== "null"
					? active_spending.comment.split(',')
					: [];
			var tags_editor = new WizardTags(
				'.tags-editor',
				{
					tags: tags,
					sort: 'asc',
					default_tags: default_tags,
					separators: ',',
					only_unique: true
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
					var tags = tags_editor.getTags();
					var comment = tags.join(', ');
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

					var serialized_tags = JSON.stringify(tags);
					buy_manager.mayBeBuy(serialized_tags);

					activity.updateWidget();
					PUSH({url: 'history.html'});

					return false;
				}
			);
		}
		function UpdateBuyEditorPage() {
			var active_buy = LoadActiveBuy();

			var edit_buy_button = $('form .edit-buy-button');
			if ($.type(active_buy) === "null") {
				$('.title').text('Add');
				$('.button-icon', edit_buy_button).removeClass('fa-save').addClass('fa-plus');
				$('.button-text', edit_buy_button).text('Add');
			} else {
				$('.title').text('Edit');
				$('.button-icon', edit_buy_button).removeClass('fa-plus').addClass('fa-save');
				$('.button-text', edit_buy_button).text('Save');
			}

			var name_editor = $('.name-editor');
			if ($.type(active_buy) !== "null") {
				name_editor.val(active_buy.name);
			}
			name_editor.focus();

			var cost_editor = $('.cost-editor');
			if ($.type(active_buy) !== "null") {
				cost_editor.val(active_buy.cost);
			}

			var status_flag = $('.status');
			if ($.type(active_buy) !== "null") {
				if (active_buy.status) {
					status_flag.addClass('active');
				}
			} else {
				status_flag.parent().hide();
			}

			edit_buy_button.click(
				function() {
					var name = name_editor.val();
					var cost = parseFloat(cost_editor.val());
					var status = status_flag.hasClass('active') ? 1 : 0;

					if ($.type(active_buy) === "null") {
						buy_manager.createBuy(name, cost);
					} else {
						buy_manager.updateBuy(active_buy.id, name, cost, status);
					}

					activity.updateWidget();
					PUSH({url: 'history.html'});

					return false;
				}
			);
		}
		function UpdateSmsPage() {
			var UPDATE_INPUT_BUTTON_TIMEOUT = 100;

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
							+ '<div class = "toggle import-flag">'
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

			var import_button = $('.import-sms-button');
			import_button.click(
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

					activity.setSetting('current_segment', 'history');
					PUSH({url: 'history.html'});
				}
			);
			var UpdateImportButton = function() {
				if ($('.sms-list .import-flag.active').length) {
					import_button.show();
				} else {
					import_button.hide();
				}
			};

			$('.sms-list').click(
				function() {
					setTimeout(UpdateImportButton, UPDATE_INPUT_BUTTON_TIMEOUT);
				}
			);
			UpdateImportButton();
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
				} else if (/\bbuy_editor\b/.test(event.detail.state.url)) {
					activity.setSetting('current_page', 'buy_editor');
					UpdateBuyEditorPage();
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
