import { CatalogUiPage } from './app.po';

describe('catalog-ui App', function() {
  let page: CatalogUiPage;

  beforeEach(() => {
    page = new CatalogUiPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
