$(document).ready(
	function() {
		var active_spending_id = null;
		var active_spending_amount = null;
		var active_spending_comment = null;

		function HideMainMenu() {
			var event = new CustomEvent('touchend');
			$('.backdrop').get(0).dispatchEvent(event);
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
			var spendings = $.parseJSON(raw_spendings);
			spendings.map(
		        function(spending) {
					spending_list.append(
						'<li class = "table-view-cell media">'
							+ '<button class = "btn second-list-button edit-spending-button" data-spending-id = "' + spending.id + '"><i class = "fa fa-pencil"></i></button>'
							+ '<button class = "btn remove-spending-button" data-spending-id = "' + spending.id + '"><i class = "fa fa-trash"></i></button>'
							+ '<span class = "media-object pull-left">'
							    + '<i class = "fa fa-'
							        + (spending.amount > 0
								        ? 'shopping-cart'
									    : 'money')
									+ ' fa-2x"></i>'
							    + '</span>'
							+ '<div class = "media-body">'
								+ '<p><strong><span class = "date-view">' + spending.date + '</span>:</strong></p>'
				                + '<p>'
							        + '<span class = "amount-view">' + spending.amount + '</span> <i class = "fa fa-ruble"></i>'
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
						spending_manager.deleteSpending(parseInt(active_spending_id));
						active_spending_id = null;

						PUSH({url: 'history.html'});
					}
				}
			);
			var remove_dialog_date_view = $('.date-view', remove_dialog);
			var remove_dialog_amount_view = $('.amount-view', remove_dialog);
			var remove_dialog_comment_view = $('.comment-view', remove_dialog);

			$('.edit-spending-button', spending_list).click(
				function() {
					var button = $(this);
					active_spending_id = button.data('spending-id');

					var list_item = button.parent();
					active_spending_amount = $('.amount-view', list_item).text();
					active_spending_comment = $('.comment-view', list_item).text();

					PUSH({url: 'editor.html'});
				}
			);
			$('.remove-spending-button', spending_list).click(
				function() {
					var button = $(this);
					active_spending_id = button.data('spending-id');

					var list_item = button.parent();
					var date = $('.date-view', list_item).text();
					var amount = $('.amount-view', list_item).text();
					var comment = $('.comment-view', list_item).text();

					remove_dialog_date_view.text(date);
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
					HideMainMenu();
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

			$('header .edit-spending-button').click(
		        function() {
			        var amount = parseFloat(amount_editor.val());
			    	amount_editor.val('');

				    var comment = comment_editor.val();
				    comment_editor.val('');

					if ($.type(spending_id) === "null") {
				    	spending_manager.createSpending(amount, comment);
					} else {
						spending_manager.updateSpending(parseInt(spending_id), amount, comment);
					}
				    
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

