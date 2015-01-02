$(document).ready(
	function() {
		function UpdateSpendingList() {
			var spendings_sum = spending_manager.getSpendingsSum();
			$('.spendings-sum-view').text(spendings_sum);

			var spending_list = $('.spending-list');
			spending_list.empty();

			var raw_spendings = spending_manager.getAllSpendings();
			var spendings = $.parseJSON(raw_spendings);
			spendings.map(
				function(spending) {
					spending_list.append(
						'<li class = "table-view-cell media">'
							+ '<span class = "media-object pull-left">'
								+ '<i class = "fa fa-'
									+ (spending.amount > 0
										? 'shopping-cart'
										: 'money')
									+ ' fa-2x"></i>'
								+ '</span>'
							+ '<div class = "media-body">'
								+ '<p>'
									+ '<strong>' + spending.date + ':</strong>'
								+ '</p>'
								+ '<p>'
									+ spending.amount + ' '
										+ '<i class = "fa fa-ruble"></i>'
									+ (spending.comment.length
										? ' &mdash; '
											+ '<em>'
												+ spending.comment
											+ '</em>'
										: '')
								+ '.</p>'
							+ '</div>'
						+ '</li>'
					);
				}
			);
		}
		function UpdateControlButtons(amount_editor) {
			$('.add-spending-button').click(
				function() {
					var amount = parseFloat(amount_editor.val());
					amount_editor.val('');

					var comment_editor = $('.comment-editor');
					var comment = comment_editor.val();
					comment_editor.val('');

					spending_manager.addSpending(amount, comment);

					amount_editor.focus();
					UpdateSpendingList();
				}
			);
			$('.remove-last-spending-button').click(
				function() {
					spending_manager.removeLastSpending();

					amount_editor.focus();
					UpdateSpendingList();
				}
			);
			$('.backup-button').click(
				function() {
					spending_manager.backup();
					amount_editor.focus();
				}
			);
		}
		function UpdateIndexPage() {
			var amount_editor = $('.amount-editor');
			amount_editor.focus();

			UpdateControlButtons(amount_editor);
			UpdateSpendingList();
		}

		window.addEventListener(
			'push',
			function(event) {
				if (/\bindex\b/.test(event.detail.state.url)) {
					UpdateIndexPage();
				}
			}
		);
		UpdateIndexPage();
	}
);
