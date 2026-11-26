/**
 * Details for the Impressum (§ 5 DDG) and the privacy policy (Art. 13 GDPR).
 *
 * Kept out of content/profile.json on purpose: that file is indexed into the
 * "Ask my CV" knowledge base, and the postal address should not be quoted by
 * the assistant.
 *
 * TODO: Fill in a postal address at which you can be served (ladungsfähige
 * Anschrift) – a P.O. box is not enough. Until then the site is NOT compliant.
 *
 * To keep your home address private, book an Impressum/c-o address service
 * (with mail forwarding and a power of attorney to accept legal mail) and put
 * the provider's name in `careOf` and its address below. It then renders as
 * "Ronald Nguyen / c/o <provider> / <street> / <postcode city>".
 */
export const legal = {
  /** Name of the c/o address provider; leave empty when using your own address. */
  careOf: '' as string,
  street: '[Straße und Hausnummer]',
  postalCode: '[PLZ]',
  city: '[Ort]',
  country: 'Deutschland',
  /** Optional, but a second quick contact channel besides email is recommended. */
  phone: '' as string,
  /** Date the privacy policy was last reviewed. */
  privacyUpdated: '26.11.2026',
};
