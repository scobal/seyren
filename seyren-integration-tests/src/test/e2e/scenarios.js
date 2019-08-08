'use strict';

describe('home page', function () {
    beforeEach(function () {
        browser().navigateTo('/');
    });

    it('should have a nav bar', function () {
        expect(element('div.navbar img').count()).toBe(1);
        expect(element('div.navbar ul li a').text()).toBe('Checks');
    });

    it('should have a \'Checks in an unhealthy state\' section', function () {
        expect(element('div h2:eq(0)').text()).toBe('Checks in an unhealthy state');
    });

    it('should have a \'Checks in an unhealthy state\' table', function () {
        expect(element('table:eq(0) thead tr').count()).toBe(1);
        expect(element('table:eq(0) thead tr th:eq(0)').text()).toBe('Name');
        expect(element('table:eq(0) thead tr th:eq(1)').text()).toBe('State');
        expect(element('table:eq(0) thead tr th:eq(2)').text()).toBe('Warn');
        expect(element('table:eq(0) thead tr th:eq(3)').text()).toBe('Error');
        expect(element('table:eq(0) thead tr th:eq(4)').text()).toBe('Enabled');

        expect(element('table:eq(0) tbody tr').count()).toBe(1);
        expect(element('table:eq(0) tbody tr td:eq(0) a').text()).toBe('load longterm usage');
        expect(element('table:eq(0) tbody tr td:eq(1) span:visible').text()).toBe('WARN');
        expect(element('table:eq(0) tbody tr td:eq(2)').text()).toBe('0.5');
        expect(element('table:eq(0) tbody tr td:eq(3)').text()).toBe('2.0');
        expect(element('table:eq(0) tbody tr td:eq(4) input:checked').val()).toBe('on');
    });

    it('should have a \'Recent alerts\' section', function () {
        expect(element('div h2:eq(1)').text()).toBe('Recent alerts');
    });

    it('should have a \'Recent alerts\' table', function () {
        expect(element('table:eq(1) thead tr').count()).toBe(1);
        expect(element('table:eq(1) thead tr th:eq(0)').text()).toBe('Timestamp');
        expect(element('table:eq(1) thead tr th:eq(1)').text()).toBe('Time ago');
        expect(element('table:eq(1) thead tr th:eq(2)').text()).toBe('Name');
        expect(element('table:eq(1) thead tr th:eq(3)').text()).toBe('Value');
        expect(element('table:eq(1) thead tr th:eq(4)').text()).toBe('Warn');
        expect(element('table:eq(1) thead tr th:eq(5)').text()).toBe('Error');
        expect(element('table:eq(1) thead tr th:eq(6)').text()).toBe('From');
        expect(element('table:eq(1) thead tr th:eq(7)').text()).toBe('To');

        expect(element('table:eq(1) tbody tr').count()).toBe(1);
        expect(element('table:eq(1) tbody tr td:eq(0)').text()).toMatch('[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}');
        expect(element('table:eq(1) tbody tr td:eq(1)').text()).toMatch('^.*ago$');
        expect(element('table:eq(1) tbody tr td:eq(2) a').text()).toBe('load longterm usage');
        expect(element('table:eq(1) tbody tr td:eq(3)').text()).toBe('0.8');
        expect(element('table:eq(1) tbody tr td:eq(4)').text()).toBe('0.5');
        expect(element('table:eq(1) tbody tr td:eq(5)').text()).toBe('2');
        expect(element('table:eq(1) tbody tr td:eq(6) span:visible').text()).toBe('WARN');
        expect(element('table:eq(1) tbody tr td:eq(7) span:visible').text()).toBe('OK');
    });

    it('should have a footer', function () {
        expect(element('footer').count()).toBe(1);
        expect(element('footer a').count()).toBe(1);
        expect(element('footer a').attr('href')).toBe('https://github.com/scobal/seyren');
        expect(element('footer a').text()).toBe('Seyren');
    });

    it('click on table row \'Checks in an unhealthy state\'', function () {
        element('table:eq(0) tbody tr').click();
        expect(browser().location().url()).toEqual('/checks/5205121fccf2a07eacba64da');
    });

    it('click on table row \'Recent alerts\'', function () {
        element('table:eq(1) tbody tr').click();
        expect(browser().location().url()).toEqual('/checks/5205121fccf2a07eacba64da');
    });
});

describe('checks page', function () {
    beforeEach(function () {
        browser().navigateTo('/#/checks');
    });

    it('should have a \'Checks\' section', function () {
        expect(element('h1').count()).toBe(1);
        expect(element('h1').text()).toBe('Checks');
    });

    it('should have a \'filter\' input', function () {
        expect(element('input[name="filter"]').count()).toBe(1);
    });

    it('should have a \'Checks\' table', function () {
        expect(element('table').count()).toBe(1);

        expect(element('table thead tr').count()).toBe(1);
        expect(element('table thead tr th:eq(0)').text()).toBe('Name');
        expect(element('table thead tr th:eq(1)').text()).toBe('State');
        expect(element('table thead tr th:eq(2)').text()).toBe('Warn');
        expect(element('table thead tr th:eq(3)').text()).toBe('Error');
        expect(element('table thead tr th:eq(4)').text()).toBe('Enabled');

        expect(element('table tbody tr').count()).toBe(1);
        expect(element('table tbody tr td:eq(0) a').text()).toBe('load longterm usage');
        expect(element('table tbody tr td:eq(1) span:visible').text()).toBe('WARN');
        expect(element('table tbody tr td:eq(2)').text()).toBe('0.5');
        expect(element('table tbody tr td:eq(3)').text()).toBe('2.0');
        expect(element('table tbody tr td:eq(4) input:checked').val()).toBe('on');
    });

    it('click on table row', function () {
        element('table tbody tr').click();
        expect(browser().location().url()).toEqual('/checks/5205121fccf2a07eacba64da');
    });
});

describe('check page', function () {
    beforeEach(function () {
        browser().navigateTo('/#/checks/5205121fccf2a07eacba64da');
    });

    it('should have a \'Details\' section', function () {
        expect(element('h2:eq(0)').count()).toBe(1);
        expect(element('h2:eq(0)').text()).toBe('Details [edit]');
    });

    it('should have a \'Subscriptions\' section', function () {
        expect(element('h2:eq(1)').count()).toBe(1);
        expect(element('h2:eq(1)').text()).toBe('Subscriptions');
        expect(element('div.col-lg-12:eq(2) div').text()).toBe('This check has no subscriptions');
    });

    it('should have a \'Details\' informations', function () {
        expect(element('div.col-lg-6 div.col-lg-10').count()).toBe(12);

        expect(element('div.col-lg-6 div.detail-form:eq(0) label').text()).toBe('Name:');
        expect(element('div.col-lg-6 div.detail-form:eq(0) p').text()).toBe('load longterm usage');

        expect(element('div.col-lg-6 div.detail-form:eq(1) label').text()).toBe('Description:');
        expect(element('div.col-lg-6 div.detail-form:eq(1) p').text()).toBe('Load longterm of host host1');

        expect(element('div.col-lg-6 div.detail-form:eq(2) label').text()).toBe('State:');
        expect(element('div.col-lg-6 div.detail-form:eq(2) p span:visible').text()).toBe('WARN');

        expect(element('div.col-lg-6 div.detail-form:eq(3) label').text()).toBe('Target:');
        expect(element('div.col-lg-6 div.detail-form:eq(3) p').text()).toBe('prod.host1.load.longterm');

        expect(element('div.col-lg-6 div.detail-form:eq(4) label').text()).toBe('From:');
        expect(element('div.col-lg-6 div.detail-form:eq(4) p').text()).toBe('');

        expect(element('div.col-lg-6 div.detail-form:eq(5) label').text()).toBe('Until:');
        expect(element('div.col-lg-6 div.detail-form:eq(5) p').text()).toBe('');

        expect(element('div.col-lg-6 div.detail-form:eq(6) label').text()).toBe('Warn:');
        expect(element('div.col-lg-6 div.detail-form:eq(6) p').text()).toBe('0.5');

        expect(element('div.col-lg-6 div.detail-form:eq(7) label').text()).toBe('Error:');
        expect(element('div.col-lg-6 div.detail-form:eq(7) p').text()).toBe('2.0');

        expect(element('div.col-lg-6 div.detail-form:eq(8) label').text()).toBe('Enabled:');
        expect(element('div.col-lg-6 div.detail-form:eq(8) p input:checked').val()).toBe('on');

        expect(element('div.col-lg-6 div.detail-form:eq(9) label').text()).toBe('Live:');
        expect(element('div.col-lg-6 div.detail-form:eq(9) p input:not(:checked)').val()).toBe('on');

        expect(element('div.col-lg-6 div.detail-form:eq(10) label').text()).toBe('Allow no data:');
        expect(element('div.col-lg-6 div.detail-form:eq(10) p input:not(:checked)').val()).toBe('on');

        expect(element('div.col-lg-6 div.detail-form:eq(11) label').text()).toBe('Last check:');
        expect(element('div.col-lg-6 div.detail-form:eq(11) p').text()).toMatch('[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}');
    });

    it('should have \'Graphs\' thumbnail', function () {
        expect(element('div.thumbnail a').count()).toBe(4);

        expect(element('div.thumbnail a:eq(0)').attr('href')).toBe('./api/checks/5205121fccf2a07eacba64da/image/?&width=1200&height=350&from=-15Minutes');
        expect(element('div.thumbnail a:eq(1)').attr('href')).toBe('./api/checks/5205121fccf2a07eacba64da/image/?&width=1200&height=350&from=-60Minutes');
        expect(element('div.thumbnail a:eq(2)').attr('href')).toBe('./api/checks/5205121fccf2a07eacba64da/image/?&width=1200&height=350&from=-1440Minutes');
        expect(element('div.thumbnail a:eq(3)').attr('href')).toBe('./api/checks/5205121fccf2a07eacba64da/image/?&width=1200&height=350&from=-10080Minutes');

        // we use toContain to bypass uniq attribute
        expect(element('div.thumbnail a:eq(0) img').attr('src')).toContain('./api/checks/5205121fccf2a07eacba64da/image/?&width=365&height=70&from=-15Minutes&hideLegend=true&hideAxes=true');
        expect(element('div.thumbnail a:eq(1) img').attr('src')).toContain('./api/checks/5205121fccf2a07eacba64da/image/?&width=365&height=70&from=-60Minutes&hideLegend=true&hideAxes=true');
        expect(element('div.thumbnail a:eq(2) img').attr('src')).toContain('./api/checks/5205121fccf2a07eacba64da/image/?&width=365&height=70&from=-1440Minutes&hideLegend=true&hideAxes=true');
        expect(element('div.thumbnail a:eq(3) img').attr('src')).toContain('./api/checks/5205121fccf2a07eacba64da/image/?&width=365&height=70&from=-10080Minutes&hideLegend=true&hideAxes=true');
    });

    it('should have \'Alerts\' section', function () {
        expect(element('h2:eq(2)').text()).toContain('Alerts');

        expect(element('h2:eq(2) div button').text()).toContain('Delete alerts');
        expect(element('h2:eq(2) div ul li:eq(0) a').text()).toBe('Older than a day');
        expect(element('h2:eq(2) div ul li:eq(1) a').text()).toBe('Older than a week');
        expect(element('h2:eq(2) div ul li:eq(2) a').text()).toBe('All');
        
        expect(element('table:eq(1) thead tr').count()).toBe(1);
        expect(element('table:eq(1) thead tr th:eq(0)').text()).toBe('Timestamp');
        expect(element('table:eq(1) thead tr th:eq(1)').text()).toBe('Time ago');
        expect(element('table:eq(1) thead tr th:eq(2)').text()).toBe('Target');
        expect(element('table:eq(1) thead tr th:eq(3)').text()).toBe('Value');
        expect(element('table:eq(1) thead tr th:eq(4)').text()).toBe('Warn level');
        expect(element('table:eq(1) thead tr th:eq(5)').text()).toBe('Error level');
        expect(element('table:eq(1) thead tr th:eq(6)').text()).toBe('From');
        expect(element('table:eq(1) thead tr th:eq(7)').text()).toBe('To');

        expect(element('table:eq(1) tbody tr').count()).toBe(1);
        expect(element('table:eq(1) tbody tr td:eq(0)').text()).toMatch('[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}');
        expect(element('table:eq(1) tbody tr td:eq(1)').text()).toMatch('^.*ago$');
        expect(element('table:eq(1) tbody tr td:eq(2)').text()).toBe('prod.host1.load.longterm');
        expect(element('table:eq(1) tbody tr td:eq(3)').text()).toBe('0.8');
        expect(element('table:eq(1) tbody tr td:eq(4)').text()).toBe('0.5');
        expect(element('table:eq(1) tbody tr td:eq(5)').text()).toBe('2');
        expect(element('table:eq(1) tbody tr td:eq(6) span:visible').text()).toBe('WARN');
        expect(element('table:eq(1) tbody tr td:eq(7) span:visible').text()).toBe('OK');
    });

});

describe('edit check', function () {
    beforeEach(function () {
        browser().navigateTo('/#/checks/5205121fccf2a07eacba64da');
    });

    it('edit and submit check', function () {
        expect(element('div.col-lg-6 div.col-lg-10').count()).toBe(12);

        expect(element('a:contains("edit")').count()).toBe(1);
        expect(element('div#editCheckModal:visible').count()).toBe(0);

        element('a:contains("edit")').click();

        expect(element('div#editCheckModal:visible').count()).toBe(1);
        expect(element('div#editCheckModal h3:visible').text()).toBe('Edit check');

        expect(element('button#updateCheckButton:visible:enabled').count()).toEqual(1);
        expect(element('button#createCheckButton:visible').count()).toEqual(0);
        expect(element('button#cancelCheckButton:visible:enabled').count()).toEqual(1);
        input('check\\.description').enter('New description');
        element('button#updateCheckButton:visible:enabled', 'Save changes').click();

        expect(element('div#editCheckModal:visible').count()).toBe(0);
        expect(element('div.col-lg-6 div.detail-form:eq(1) p').text()).toBe('New description');
    });

});

describe('add subscription', function () {
    beforeEach(function () {
        browser().navigateTo('/#/checks/5205121fccf2a07eacba64da');
    });

    it('add subscription', function () {
        expect(element('button#editSubscaddriptionButton').count()).toEqual(1);
        expect(element('div#editSubscriptionModal:visible').count()).toBe(0);

        element('button#editSubscaddriptionButton').click();

        expect(element('div#editSubscriptionModal:visible').count()).toBe(1);
        expect(element('button#updateSubscriptionButton:visible').count()).toEqual(0);
        expect(element('button#createSubscriptionButton:visible:enabled').count()).toEqual(0);
        expect(element('button#cancelSubscriptionButton:visible:enabled').count()).toEqual(1);

        expect(element('div#editSubscriptionModal h3:visible').text()).toBe('Add subscription');
        input('subscription\\.target').enter('my target');

        expect(element('button#updateSubscriptionButton:visible').count()).toEqual(0);
        expect(element('button#createSubscriptionButton:visible:enabled').count()).toEqual(1);
        expect(element('button#cancelSubscriptionButton:visible:enabled').count()).toEqual(1);

        element('button#createSubscriptionButton:visible:enabled', 'Add').click();

        expect(element('div#editSubscriptionModal:visible').count()).toBe(0);

        expect(element('table:eq(0) thead tr').count()).toBe(1);
        expect(element('table:eq(0) thead tr th:eq(0)').text()).toBe('Target');
        expect(element('table:eq(0) thead tr th:eq(1)').text()).toBe('Type');
        expect(element('table:eq(0) thead tr th:eq(2)').text()).toBe('Notify on');
        expect(element('table:eq(0) thead tr th:eq(3)').text()).toBe('Days');
        expect(element('table:eq(0) thead tr th:eq(4)').text()).toBe('Time');
        expect(element('table:eq(0) thead tr th:eq(5)').text()).toBe('On?');
        
        expect(element('table:eq(0) tbody tr').count()).toBe(1);
        expect(element('table:eq(0) tbody tr td:eq(0)').text()).toBe('my target');
        expect(element('table:eq(0) tbody tr td:eq(1)').text()).toBe('EMAIL');
        expect(element('table:eq(0) tbody tr td:eq(2) span:visible').count()).toBe(3);
        expect(element('table:eq(0) tbody tr td:eq(3) span:visible').count()).toBe(7);
        expect(element('table:eq(0) tbody tr td:eq(4)').text()).toBe('0000 to 2359');
        expect(element('table:eq(0) tbody tr td:eq(5) input:checked').val()).toBe('on');
        expect(element('table:eq(0) tbody tr td:eq(6) button').text()).toBe('Delete');
        expect(element('table:eq(0) tbody tr td:eq(7) button').text()).toBe('Edit');
    });
});

describe('edit subscription', function () {
    beforeEach(function () {
        browser().navigateTo('/#/checks/5205121fccf2a07eacba64da');
    });

    it('edit subscription', function () {
        expect(element('div#editSubscriptionModal:visible').count()).toBe(0);
        expect(element('table:eq(0) tbody tr').count()).toBe(1);
        element('table:eq(0) tbody tr td:eq(7) button').click();

        expect(element('div#editSubscriptionModal:visible').count()).toBe(1);
        expect(element('button#updateSubscriptionButton:visible:enabled').count()).toEqual(1);
        expect(element('button#createSubscriptionButton:visible').count()).toEqual(0);
        expect(element('button#cancelSubscriptionButton:visible:enabled').count()).toEqual(1);

        expect(element('div#editSubscriptionModal h3:visible').text()).toBe('Edit subscription');
        input('subscription\\.target').enter('my target 2');

        expect(element('button#updateSubscriptionButton:visible:enabled').count()).toEqual(1);
        expect(element('button#createSubscriptionButton:visible').count()).toEqual(0);
        expect(element('button#cancelSubscriptionButton:visible:enabled').count()).toEqual(1);

        element('button#updateSubscriptionButton:visible:enabled', 'Save changes').click();

        expect(element('editSubscriptionModal:visible').count()).toBe(0);
        expect(element('table:eq(0) tbody tr td:eq(0)').text()).toBe('my target 2');
        expect(element('table:eq(0) tbody tr').count()).toBe(1);
    });
});

describe('delete subscription', function () {
    beforeEach(function () {
        browser().navigateTo('/#/checks/5205121fccf2a07eacba64da');
    });

    it('delete subscription', function () {
        expect(element('div#editSubscriptionModal:visible').count()).toBe(0);
        expect(element('table:eq(0) tbody tr').count()).toBe(1);
        element('table:eq(0) tbody tr td:eq(6) button').click();
        expect(element('table:eq(0) tbody tr').count()).toBe(0);

    });
});

describe('delete check', function () {
    beforeEach(function () {
        browser().navigateTo('/#/checks/5205121fccf2a07eacba64da');
    });

    it('delete check', function () {
        expect(element('button#deleteCheckButton:visible:enabled').count()).toEqual(1);
        expect(element('div#confirmCheckDeleteModal:visible').count()).toBe(0);
        expect(element('button#confirmDeleteCheckButton:visible').count()).toBe(0);

        element('button#deleteCheckButton:visible:enabled').click();

        expect(element('div#confirmCheckDeleteModal:visible').count()).toBe(1);
        expect(element('button#confirmDeleteCheckButton:visible').count()).toBe(1);

        element('button#confirmDeleteCheckButton:visible:enabled').click();
        expect(browser().location().url()).toEqual('/checks');
        expect(element('table tbody tr').count()).toBe(0);
    });

});

describe('create new check', function () {
    beforeEach(function () {
        browser().navigateTo('/#/checks');
    });

    it('should have a \'Check\' modal dialog', function () {
        expect(element('table tbody tr').count()).toBe(0);
        expect(element('div#editCheckModal:visible').count()).toBe(0);
        expect(element('button#createNewCheckButton').count()).toBe(1);

        element('button#createNewCheckButton', 'Create Check').click();
        expect(element('div#editCheckModal:visible').count()).toBe(1);

        expect(element('button#createCheckButton:visible:disabled').count()).toEqual(1);
        expect(element('button#cancelCheckButton:visible:enabled').count()).toEqual(1);

        expect(element('div#editCheckModal h3:visible').text()).toBe('Create check');
        input('check\\.name').enter('Name of karma.metric');
        input('check\\.description').enter('Description of karma.metric');
        input('check\\.target').enter('karma.metric');
        input('check\\.warn').enter('2.0');
        input('check\\.error').enter('4.0');
        input('check\\.enabled').check();

        expect(element('div#editCheckModal img').attr('src')).toBe('./api/chart/karma.metric/?&width=365&height=70&from=-1day&warn=2.0&error=4.0&hideLegend=true');

        expect(element('button#createCheckButton:visible:enabled').count()).toEqual(1);
        expect(element('button#cancelCheckButton:visible:enabled').count()).toEqual(1);

        element('button#createCheckButton:visible:enabled', 'Create Check').click();

        expect(element('div#editCheckModal:visible').count()).toBe(0);

        expect(element('table tbody tr').count()).toBe(1);

    });

});

