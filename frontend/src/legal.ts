/**
 * Details for the Impressum (§ 5 DDG) and the privacy policy (Art. 13 GDPR).
 *
 * Kept out of content/profile.json on purpose: that file is indexed into the
 * "Ask my CV" knowledge base, and the postal address should not be quoted by
 * the assistant.
 *
 * TODO: Fill in a postal address at which you can be served (ladungsfähige
 * Anschrift) – a P.O. box is not enough. Until then the site is NOT compliant.
 */
export const legal = {
  street: '[Straße und Hausnummer]',
  postalCode: '[PLZ]',
  city: 'Jena',
  country: 'Deutschland',
  /** Optional, but a second quick contact channel besides email is recommended. */
  phone: '' as string,
  /** Date the privacy policy was last reviewed. */
  privacyUpdated: '26.11.2026',
};
