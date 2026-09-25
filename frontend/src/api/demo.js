/*
 * Front-end demo mode.
 *
 * When the app is published to GitHub Pages there is no backend, so every
 * API call would 404. This module provides an in-browser axios adapter that
 * serves seeded data and a fake auth flow, letting visitors actually browse
 * listings, "log in" with the demo accounts, and click through the UI with
 * no server. Enabled via VITE_RENOVA_DEMO=true at build time.
 */

const asset = (name) => `${import.meta.env.BASE_URL}demo-images/products/${name}`
const now = Date.now()
const daysAgo = (d) => new Date(now - d * 86400000).toISOString()

const users = {
  1: {
    id: 1,
    email: 'ava@renova.local',
    displayName: 'Ava Thompson',
    avatarUrl: null,
    location: 'Auckland',
    role: 'USER',
    bio: 'Pre-loved fashion and homeware curator. Everything cleaned and checked.',
    averageRating: 4.8,
    ratingCount: 23,
    memberSince: daysAgo(320)
  },
  2: {
    id: 2,
    email: 'liam@renova.local',
    displayName: 'Liam Carter',
    avatarUrl: null,
    location: 'Wellington',
    role: 'USER',
    bio: 'Sports gear and home odds and ends. Happy to bundle.',
    averageRating: 4.6,
    ratingCount: 15,
    memberSince: daysAgo(210)
  },
  3: {
    id: 3,
    email: 'maya@renova.local',
    displayName: 'Maya Flores',
    avatarUrl: null,
    location: 'Christchurch',
    role: 'USER',
    bio: 'Vintage finds and camera gear. Fast shipper.',
    averageRating: 4.9,
    ratingCount: 11,
    memberSince: daysAgo(160)
  },
  4: {
    id: 4,
    email: 'theo@renova.local',
    displayName: 'Theo Novak',
    avatarUrl: null,
    location: 'Hamilton',
    role: 'USER',
    bio: 'Always open to a fair offer.',
    averageRating: 4.5,
    ratingCount: 8,
    memberSince: daysAgo(95)
  },
  5: {
    id: 5,
    email: 'ivy@renova.local',
    displayName: 'Ivy Bennett',
    avatarUrl: null,
    location: 'Dunedin',
    role: 'USER',
    bio: 'Plants, ceramics, and cosy knitwear.',
    averageRating: 5.0,
    ratingCount: 6,
    memberSince: daysAgo(70)
  }
}

const summaryUser = (u) => ({
  id: u.id,
  email: u.email,
  displayName: u.displayName,
  avatarUrl: u.avatarUrl,
  location: u.location,
  role: u.role
})
const publicUser = (u) => ({
  id: u.id,
  displayName: u.displayName,
  avatarUrl: u.avatarUrl,
  bio: u.bio,
  location: u.location,
  averageRating: u.averageRating,
  ratingCount: u.ratingCount,
  memberSince: u.memberSince
})

const categories = [
  { id: 1, name: "Women's Fashion", slug: 'women', icon: '👗', sortOrder: 1 },
  { id: 2, name: "Men's Fashion", slug: 'men', icon: '👔', sortOrder: 2 },
  { id: 3, name: 'Shoes', slug: 'shoes', icon: '👟', sortOrder: 3 },
  { id: 4, name: 'Bags & Accessories', slug: 'bags', icon: '👜', sortOrder: 4 },
  { id: 5, name: 'Home', slug: 'home', icon: '🏠', sortOrder: 5 },
  { id: 6, name: 'Sports', slug: 'sports', icon: '🏀', sortOrder: 6 },
  { id: 7, name: 'Collectibles', slug: 'collectibles', icon: '📷', sortOrder: 7 }
]
const categoryById = (id) => categories.find((c) => c.id === id) || categories[0]

const rawListings = [
  { id: 1, sellerId: 1, categoryId: 1, title: 'Chocolate Satin Wrap Dress', price: 68, originalPrice: 180, condition: 'LIKE_NEW', status: 'ACTIVE', loc: 'Auckland', views: 142, favs: 18, age: 3,
    imgs: ['satin-wrap-dress-1.jpg', 'satin-wrap-dress-2.jpg', 'satin-wrap-dress-3.jpg', 'satin-wrap-dress-4.jpg', 'satin-wrap-dress-5.jpg'],
    desc: 'Chocolate-brown satin shirt dress with a deep V neckline, ruched wrap front and long sleeves with buttoned cuffs. Worn twice to weddings and dry-cleaned since - it drapes beautifully and has a subtle sheen in person.\n\n• Size: NZ 10, runs slightly small (best for 8-10)\n• Material: Polyester satin, unlined\n• Measurements: Bust 88 cm, waist 74 cm, length 98 cm (shoulder to hem)\n• Condition: No pulls, marks or loose threads; all cuff buttons present\n• Care: Cool hand wash or dry clean\n• Pickup / shipping: Tracked courier within 2 working days, or pickup in Ponsonby, Auckland' },
  { id: 2, sellerId: 1, categoryId: 4, title: 'Tan Leather Messenger Bag', price: 95, originalPrice: 240, condition: 'GOOD', status: 'ACTIVE', loc: 'Auckland', views: 98, favs: 12, age: 6,
    imgs: ['tan-leather-messenger-1.jpg', 'tan-leather-messenger-2.jpg', 'tan-leather-messenger-3.jpg'],
    desc: 'Full-grain tan leather messenger with two front flap pockets, a zipped main compartment and a detachable adjustable shoulder strap. The leather has a warm, glossy finish that is starting to develop a nice patina.\n\n• Measurements: 36 x 26 x 10 cm; strap adjusts 90-140 cm\n• Material: Full-grain cowhide, contrast stitching, gunmetal hardware\n• Condition: Light scuffing on the base corners; all zips run smoothly; interior clean\n• Included: Shoulder strap and dust bag\n• Pickup / shipping: Free tracked shipping NZ-wide' },
  { id: 3, sellerId: 1, categoryId: 1, title: 'Black Leather Biker Jacket', price: 110, originalPrice: 320, condition: 'GOOD', status: 'ACTIVE', loc: 'Auckland', views: 131, favs: 21, age: 9,
    imgs: ['black-biker-jacket-1.jpg', 'black-biker-jacket-2.jpg', 'black-biker-jacket-3.jpg', 'black-biker-jacket-4.jpg', 'black-biker-jacket-5.jpg'],
    desc: 'Classic asymmetric-zip biker jacket in soft black leather with snap-down lapels, zipped cuffs and a belted hem. Photos show the chunky silver zips and the grain close up.\n\n• Size: Women\'s M (NZ 12)\n• Measurements: Pit to pit 51 cm, length 56 cm, sleeve 61 cm\n• Material: Lambskin leather, polyester lining\n• Condition: Natural creasing at the elbows; lining intact; every zip and snap works\n• Care: Wipe clean; condition twice a year\n• Pickup / shipping: Tracked courier, or try it on at pickup in Auckland CBD' },
  { id: 4, sellerId: 1, categoryId: 4, title: 'Crystal Leaf Wreath Earrings', price: 28, originalPrice: 79, condition: 'LIKE_NEW', status: 'RESERVED', loc: 'Auckland', views: 51, favs: 9, age: 12,
    imgs: ['crystal-leaf-earrings-1.jpg', 'crystal-leaf-earrings-2.jpg', 'crystal-leaf-earrings-3.jpg'],
    desc: 'Sparkly wreath-shaped stud earrings made of marquise-cut crystals arranged like leaves. They catch the light really well - worn once for an evening event.\n\n• Measurements: About 2.5 cm across\n• Material: Silver-tone brass, cubic zirconia, sterling silver posts\n• Condition: All stones present and secure; posts straight\n• Included: Original gift box\n• Pickup / shipping: Posted in a padded envelope with tracking' },
  { id: 5, sellerId: 1, categoryId: 4, title: 'Gold-tone Jhumka Earrings (4 pairs)', price: 45, originalPrice: 140, condition: 'NEW', status: 'ACTIVE', loc: 'Auckland', views: 63, favs: 11, age: 15,
    imgs: ['jhumka-earrings-bundle-1.jpg', 'jhumka-earrings-bundle-2.jpg', 'jhumka-earrings-bundle-3.jpg', 'jhumka-earrings-bundle-4.jpg'],
    desc: 'Bundle of four pairs of statement jhumka earrings - each photo shows one pair. Temple-style gold-tone bells with red and green stone accents and bead fringes, bought for a family wedding and never worn.\n\n• Measurements: Drops from 4.5 cm to 6 cm\n• Material: Gold-plated alloy, glass stones, faux pearls\n• Condition: New, still on their original cards\n• Included: All 4 pairs; happy to split for $14 a pair\n• Pickup / shipping: Tracked post, or pickup in Auckland' },
  { id: 6, sellerId: 2, categoryId: 3, title: 'Converse Run Star Hike High-Tops', price: 75, originalPrice: 180, condition: 'GOOD', status: 'ACTIVE', loc: 'Wellington', views: 120, favs: 14, age: 4,
    imgs: ['converse-run-star-hike-1.jpg', 'converse-run-star-hike-2.jpg', 'converse-run-star-hike-3.jpg'],
    desc: 'Converse Run Star Hike platform high-tops in black canvas with the chunky jagged sole. Comfortable and surprisingly light for the height.\n\n• Size: Women\'s US 8 / men\'s US 6 / EU 38.5\n• Material: Canvas upper, rubber platform sole (about 5 cm)\n• Condition: Worn a handful of times; soles clean with barely any wear, canvas has no stains\n• Included: Original laces; no box\n• Pickup / shipping: Double-boxed tracked courier from Wellington' },
  { id: 7, sellerId: 2, categoryId: 6, title: 'Hex Dumbbells 5 kg (pair)', price: 40, originalPrice: 80, condition: 'GOOD', status: 'ACTIVE', loc: 'Wellington', views: 66, favs: 9, age: 2,
    imgs: ['hex-dumbbells-5kg-1.jpg', 'hex-dumbbells-5kg-2.jpg', 'hex-dumbbells-5kg-3.jpg', 'hex-dumbbells-5kg-4.jpg', 'hex-dumbbells-5kg-5.jpg'],
    desc: 'A pair of 5 kg rubber-coated hex dumbbells with knurled chrome handles. The hex heads stop them rolling, so they are easy to store and safe on wooden floors.\n\n• Included: 2 x 5 kg dumbbells\n• Material: Cast iron core, rubber coating, chrome handle\n• Condition: Home use only; no rust on the handles and the rubber has no cracks\n• Pickup / shipping: Pickup in Wellington preferred, or courier at cost (heavy!)' },
  { id: 8, sellerId: 2, categoryId: 4, title: 'Burgundy Leather Backpack', price: 85, originalPrice: 220, condition: 'LIKE_NEW', status: 'ACTIVE', loc: 'Wellington', views: 58, favs: 6, age: 10,
    imgs: ['burgundy-leather-backpack-1.jpg', 'burgundy-leather-backpack-2.jpg', 'burgundy-leather-backpack-3.jpg'],
    desc: 'Oxblood leather backpack with a debossed logo, a zipped front pocket and two side pockets. Big enough for daily commuting without looking bulky.\n\n• Measurements: 40 x 30 x 13 cm; fits a 14-inch laptop\n• Material: Crazy-horse leather with canvas lining\n• Condition: Used for a few weeks; no scratches, straps and zips like new\n• Included: Dust bag\n• Pickup / shipping: Free tracked shipping' },
  { id: 9, sellerId: 2, categoryId: 5, title: 'Vintage-style Manual Coffee Grinder', price: 35, originalPrice: 69, condition: 'GOOD', status: 'ACTIVE', loc: 'Wellington', views: 39, favs: 4, age: 20,
    imgs: ['manual-coffee-grinder-1.jpg', 'manual-coffee-grinder-2.jpg', 'manual-coffee-grinder-3.jpg', 'manual-coffee-grinder-4.jpg'],
    desc: 'Hand-crank coffee grinder with a wooden base, pull-out drawer and cast-metal hopper. Adjustable burr for anything from espresso-fine to French-press coarse - and it looks great on the bench.\n\n• Measurements: 12 x 12 x 22 cm (with handle)\n• Material: Wood, cast iron burr, brass-look handle\n• Condition: Burrs cleaned; small wear marks on the drawer knob\n• Included: Wooden scoop\n• Pickup / shipping: Tracked courier or pickup in Wellington' },
  { id: 10, sellerId: 2, categoryId: 5, title: 'White Cotton Cushions (pair)', price: 30, originalPrice: 80, condition: 'GOOD', status: 'SOLD', loc: 'Wellington', views: 71, favs: 8, age: 24,
    imgs: ['white-cotton-cushions-1.jpg', 'white-cotton-cushions-2.jpg', 'white-cotton-cushions-3.jpg', 'white-cotton-cushions-4.jpg'],
    desc: 'Two plain white cushions with piped edges - an easy match for any sofa or bed. Sold, kept here so you can see past sales.\n\n• Measurements: 45 x 45 cm each\n• Material: Cotton-blend covers with hidden zips, polyester inners\n• Condition: Covers freshly washed; inners still plump\n• Pickup / shipping: Sold' },
  { id: 11, sellerId: 5, categoryId: 1, title: 'Navy Satin Asymmetric Dress', price: 58, originalPrice: 160, condition: 'LIKE_NEW', status: 'ACTIVE', loc: 'Dunedin', views: 57, favs: 9, age: 2,
    imgs: ['navy-satin-dress-1.jpg', 'navy-satin-dress-2.jpg', 'navy-satin-dress-3.jpg', 'navy-satin-dress-4.jpg', 'navy-satin-dress-5.jpg'],
    desc: 'Sleeveless midnight-navy satin dress with a high neck, a diagonal seam across the body and an asymmetric handkerchief hem. Elegant for evening events without being fussy.\n\n• Size: NZ 10\n• Measurements: Bust 90 cm, length 108 cm at the longest point\n• Material: Satin-finish polyester, lined bodice\n• Condition: Worn once; no marks or snags\n• Care: Cool hand wash, hang to dry\n• Pickup / shipping: Tracked courier from Dunedin' },
  { id: 12, sellerId: 3, categoryId: 1, title: 'Emerald Belted Skater Dress', price: 42, originalPrice: 120, condition: 'GOOD', status: 'ACTIVE', loc: 'Christchurch', views: 48, favs: 7, age: 5,
    imgs: ['emerald-belted-dress-1.jpg', 'emerald-belted-dress-2.jpg', 'emerald-belted-dress-3.jpg', 'emerald-belted-dress-4.jpg'],
    desc: 'Deep emerald dress with long balloon sleeves, a keyhole back and a fitted waist with a matching ring belt, flaring into a short skater skirt.\n\n• Size: NZ 8-10\n• Measurements: Waist 70 cm, length 88 cm\n• Material: Polyester twill with a soft sheen\n• Condition: Worn a few times; belt ring has faint marks\n• Included: Matching belt\n• Pickup / shipping: Tracked courier from Christchurch' },
  { id: 13, sellerId: 3, categoryId: 1, title: 'Classic Beige Trench Coat', price: 75, originalPrice: 240, condition: 'GOOD', status: 'ACTIVE', loc: 'Christchurch', views: 88, favs: 13, age: 7,
    imgs: ['beige-trench-coat-1.jpg', 'beige-trench-coat-2.jpg', 'beige-trench-coat-3.jpg'],
    desc: 'Double-breasted trench in a light camel beige with storm flaps, a tie belt and buckled cuffs. A wardrobe staple that works over everything from jeans to dresses.\n\n• Size: Women\'s M (NZ 12), relaxed fit\n• Measurements: Pit to pit 56 cm, length 104 cm\n• Material: Cotton gabardine, water-repellent finish\n• Condition: Dry-cleaned; slight wear on the inside collar\n• Included: Original belt\n• Pickup / shipping: Tracked courier or pickup in Christchurch' },
  { id: 14, sellerId: 5, categoryId: 4, title: 'Halo Pendant Necklace', price: 48, originalPrice: 150, condition: 'LIKE_NEW', status: 'ACTIVE', loc: 'Dunedin', views: 44, favs: 8, age: 11,
    imgs: ['halo-pendant-necklace-1.jpg', 'halo-pendant-necklace-2.jpg', 'halo-pendant-necklace-3.jpg'],
    desc: 'Cushion-shaped halo pendant set with small pavé stones on a fine box chain. A delicate everyday sparkle.\n\n• Measurements: Pendant 1.2 cm; chain 45 cm with lobster clasp\n• Material: Sterling silver, cubic zirconia\n• Condition: Polished; clasp works perfectly\n• Included: Jewellery pouch\n• Pickup / shipping: Posted with tracking in a padded envelope' },
  { id: 15, sellerId: 4, categoryId: 2, title: 'Printed Polo Shirts (bundle of 3)', price: 45, originalPrice: 150, condition: 'LIKE_NEW', status: 'ACTIVE', loc: 'Hamilton', views: 41, favs: 5, age: 13,
    imgs: ['printed-polo-bundle-1.jpg', 'printed-polo-bundle-2.jpg', 'printed-polo-bundle-3.jpg', 'printed-polo-bundle-4.jpg'],
    desc: 'Three piqué polo shirts in navy, white and pale grey, each with a small all-over print and contrast tipping on the collar and sleeves.\n\n• Size: Men\'s M (regular fit)\n• Material: Cotton-blend piqué\n• Measurements: Pit to pit 53 cm, length 71 cm\n• Condition: Worn once or twice each; no fading\n• Included: All 3 polos\n• Pickup / shipping: Tracked courier from Hamilton' },
  { id: 16, sellerId: 4, categoryId: 2, title: 'Nimble Made Dress Shirts (bundle of 3)', price: 55, originalPrice: 240, condition: 'GOOD', status: 'ACTIVE', loc: 'Hamilton', views: 52, favs: 6, age: 16,
    imgs: ['dress-shirts-bundle-1.jpg', 'dress-shirts-bundle-2.jpg', 'dress-shirts-bundle-3.jpg'],
    desc: 'Three slim-fit cotton dress shirts: crisp white, pale blue and navy micro-dot. Ideal for work - they iron easily and keep their shape.\n\n• Size: Men\'s 15.5 collar / 33 sleeve, slim fit\n• Material: 100% cotton poplin\n• Condition: Freshly laundered and pressed; no collar wear, all buttons present\n• Included: All 3 shirts\n• Pickup / shipping: Tracked courier, folded with tissue' },
  { id: 17, sellerId: 4, categoryId: 2, title: 'Acne Studios Dark Jeans', price: 85, originalPrice: 350, condition: 'GOOD', status: 'ACTIVE', loc: 'Hamilton', views: 77, favs: 12, age: 8,
    imgs: ['acne-dark-jeans-1.jpg', 'acne-dark-jeans-2.jpg', 'acne-dark-jeans-3.jpg', 'acne-dark-jeans-4.jpg'],
    desc: 'Acne Studios straight-leg jeans in a dark navy wash with copper rivets, button fly and the leather back patch.\n\n• Size: W32 L32\n• Measurements: Waist 82 cm flat x2, inseam 81 cm, leg opening 18 cm\n• Material: Cotton denim with a little stretch\n• Condition: Light fading at the thighs; hems original, no fraying\n• Pickup / shipping: Tracked courier or pickup in Hamilton' },
  { id: 18, sellerId: 3, categoryId: 2, title: 'Light-Wash Jeans (bundle of 3 pairs)', price: 48, originalPrice: 180, condition: 'GOOD', status: 'ACTIVE', loc: 'Christchurch', views: 36, favs: 4, age: 19,
    imgs: ['light-wash-jeans-bundle-1.jpg', 'light-wash-jeans-bundle-2.jpg', 'light-wash-jeans-bundle-3.jpg', 'light-wash-jeans-bundle-4.jpg'],
    desc: 'Three pairs of slim-tapered jeans in light and mid washes with whiskering and a little distressing at the knees.\n\n• Size: Men\'s W31 (all three)\n• Material: Stretch cotton denim\n• Condition: Gently worn; one pair has a faint mark on the hem\n• Included: 3 pairs; can split at $18 each\n• Pickup / shipping: Tracked courier from Christchurch' },
  { id: 19, sellerId: 4, categoryId: 2, title: 'Two-Tone Polo Shirts (pair)', price: 30, originalPrice: 100, condition: 'LIKE_NEW', status: 'ACTIVE', loc: 'Hamilton', views: 29, favs: 3, age: 21,
    imgs: ['two-tone-polo-pair-1.jpg', 'two-tone-polo-pair-2.jpg', 'two-tone-polo-pair-3.jpg'],
    desc: 'Two sporty polo shirts with striped collars: one steel blue with white panels, one white with steel-blue panels and script chest embroidery.\n\n• Size: Men\'s L\n• Material: Quick-dry polyester piqué\n• Condition: Worn once each\n• Included: Both polos\n• Pickup / shipping: Tracked courier' },
  { id: 20, sellerId: 4, categoryId: 3, title: 'Brown Leather Chelsea Boots', price: 90, originalPrice: 260, condition: 'GOOD', status: 'ACTIVE', loc: 'Hamilton', views: 96, favs: 15, age: 3,
    imgs: ['brown-chelsea-boots-1.jpg', 'brown-chelsea-boots-2.jpg', 'brown-chelsea-boots-3.jpg'],
    desc: 'Dark brown leather Chelsea boots with elastic side gussets, front and back pull tabs and a stacked leather heel. The classic Australian-style boot.\n\n• Size: Men\'s UK 9 (G fitting)\n• Material: Full-grain leather upper, leather sole\n• Condition: Resoled once; uppers conditioned and polished, light creasing across the toe\n• Pickup / shipping: Tracked courier or pickup in Hamilton' },
  { id: 21, sellerId: 3, categoryId: 3, title: 'Lacoste White Leather Sneakers', price: 55, originalPrice: 180, condition: 'LIKE_NEW', status: 'ACTIVE', loc: 'Christchurch', views: 77, favs: 12, age: 17,
    imgs: ['lacoste-white-sneakers-1.jpg', 'lacoste-white-sneakers-2.jpg', 'lacoste-white-sneakers-3.jpg', 'lacoste-white-sneakers-4.jpg'],
    desc: 'Lacoste white leather court sneakers with the green heel tab and embossed heritage logo on the side.\n\n• Size: Women\'s US 8 / EU 39\n• Material: Leather upper, rubber cupsole\n• Condition: Worn a few times; soles still clean, no creasing\n• Included: Spare white laces; no box\n• Pickup / shipping: Double-boxed tracked courier' },
  { id: 22, sellerId: 4, categoryId: 3, title: 'Dark Brown Lace-Up Boots', price: 120, originalPrice: 330, condition: 'LIKE_NEW', status: 'ACTIVE', loc: 'Hamilton', views: 83, favs: 11, age: 10,
    imgs: ['dark-brown-lace-up-boots-1.jpg', 'dark-brown-lace-up-boots-2.jpg', 'dark-brown-lace-up-boots-3.jpg', 'dark-brown-lace-up-boots-4.jpg'],
    desc: 'Cap-toe service boots in dark brown waxed leather with speed hooks and a Goodyear-welted sole. Worn twice, then they sat in the box.\n\n• Size: Men\'s US 10\n• Material: Waxed full-grain leather, rubber heel, leather midsole\n• Condition: As new - tiny crease at the flex point only\n• Included: Original box and tissue\n• Pickup / shipping: Free tracked shipping' },
  { id: 23, sellerId: 4, categoryId: 3, title: 'Tan Pebble-Grain Brogues', price: 70, originalPrice: 210, condition: 'GOOD', status: 'RESERVED', loc: 'Hamilton', views: 68, favs: 9, age: 12,
    imgs: ['tan-pebble-brogues-1.jpg', 'tan-pebble-brogues-2.jpg', 'tan-pebble-brogues-3.jpg', 'tan-pebble-brogues-4.jpg'],
    desc: 'Tan pebble-grain leather derby brogues with waxed laces and punched detailing along the seams. Great with chinos or a suit.\n\n• Size: Men\'s UK 8\n• Material: Pebble-grain calf leather, leather lining\n• Condition: Worn to a few events; conditioned and polished, small scuff on one heel\n• Pickup / shipping: Tracked courier' },
  { id: 24, sellerId: 5, categoryId: 3, title: 'Leather Two-Strap Sandals', price: 38, originalPrice: 110, condition: 'GOOD', status: 'ACTIVE', loc: 'Dunedin', views: 46, favs: 7, age: 6,
    imgs: ['leather-two-strap-sandals-1.jpg', 'leather-two-strap-sandals-2.jpg', 'leather-two-strap-sandals-3.jpg', 'leather-two-strap-sandals-4.jpg'],
    desc: 'Black leather two-strap sandals with adjustable buckles on a contoured cork footbed. The footbed has already moulded nicely.\n\n• Size: EU 38\n• Material: Leather straps, cork-latex footbed, EVA sole\n• Condition: One summer of wear; straps unmarked, soles with light wear\n• Pickup / shipping: Tracked courier from Dunedin' },
  { id: 25, sellerId: 1, categoryId: 4, title: 'Tan Leather Sling Bag', price: 55, originalPrice: 150, condition: 'LIKE_NEW', status: 'ACTIVE', loc: 'Auckland', views: 39, favs: 5, age: 14,
    imgs: ['tan-leather-sling-1.jpg', 'tan-leather-sling-2.jpg', 'tan-leather-sling-3.jpg'],
    desc: 'Slim vertical sling bag in glossy tan leather with a zipped front pocket, a back slip pocket and D-rings for the strap. Fits phone, wallet and keys.\n\n• Measurements: 18 x 26 x 6 cm\n• Material: Full-grain leather, contrast stitching\n• Condition: Used a couple of times; no marks\n• Included: Adjustable strap\n• Pickup / shipping: Tracked post' },
  { id: 26, sellerId: 3, categoryId: 4, title: 'Brown Leather Backpack', price: 80, originalPrice: 220, condition: 'GOOD', status: 'ACTIVE', loc: 'Christchurch', views: 55, favs: 8, age: 18,
    imgs: ['brown-leather-backpack-1.jpg', 'brown-leather-backpack-2.jpg', 'brown-leather-backpack-3.jpg'],
    desc: 'Chestnut crazy-horse leather backpack with a debossed logo patch, front zip pocket and two side pockets. The leather marks and self-heals, so it ages beautifully.\n\n• Measurements: 40 x 30 x 13 cm\n• Material: Crazy-horse leather, canvas lining\n• Condition: Light rub marks consistent with the leather style; zips and straps perfect\n• Pickup / shipping: Tracked courier from Christchurch' },
  { id: 27, sellerId: 3, categoryId: 4, title: 'Brown Leather Bifold Wallet', price: 25, originalPrice: 80, condition: 'LIKE_NEW', status: 'ACTIVE', loc: 'Christchurch', views: 37, favs: 5, age: 22,
    imgs: ['brown-bifold-wallet-1.jpg', 'brown-bifold-wallet-2.jpg', 'brown-bifold-wallet-3.jpg', 'brown-bifold-wallet-4.jpg'],
    desc: 'Slim bifold wallet in brown pull-up leather with six card slots and a full-length note pocket. Used for a month before I switched to a phone-case wallet.\n\n• Measurements: 11 x 9 cm closed\n• Material: Pull-up cowhide leather\n• Condition: Tiny scratch on the back, otherwise like new\n• Pickup / shipping: Tracked post' },
  { id: 28, sellerId: 4, categoryId: 4, title: 'Pop-Up Card Wallet', price: 35, originalPrice: 95, condition: 'GOOD', status: 'ACTIVE', loc: 'Hamilton', views: 42, favs: 6, age: 25,
    imgs: ['pop-up-card-wallet-1.jpg', 'pop-up-card-wallet-2.jpg', 'pop-up-card-wallet-3.jpg', 'pop-up-card-wallet-4.jpg'],
    desc: 'Cognac leather wallet with an aluminium pop-up card case - press the lever and your cards fan out. Snap-closure note pocket at the back.\n\n• Measurements: 10 x 7 x 2 cm\n• Material: Leather wrap, aluminium RFID-blocking card case\n• Condition: Light patina on the leather; mechanism works smoothly\n• Pickup / shipping: Tracked post' },
  { id: 29, sellerId: 3, categoryId: 4, title: 'Green Square-Frame Sunglasses', price: 40, originalPrice: 150, condition: 'LIKE_NEW', status: 'ACTIVE', loc: 'Christchurch', views: 89, favs: 14, age: 4,
    imgs: ['green-square-sunglasses-1.jpg', 'green-square-sunglasses-2.jpg', 'green-square-sunglasses-3.jpg', 'green-square-sunglasses-4.jpg'],
    desc: 'Translucent bottle-green acetate square frames with dark grey polarised lenses and silver rivet details.\n\n• Measurements: Lens 52 mm, bridge 20 mm, arms 145 mm\n• Material: Acetate frame, polarised lenses (UV400)\n• Condition: No scratches on the lenses; hinges tight\n• Included: Hard case and cleaning cloth\n• Pickup / shipping: Tracked post' },
  { id: 30, sellerId: 5, categoryId: 5, title: 'Mustard Wingback Armchair', price: 180, originalPrice: 549, condition: 'GOOD', status: 'ACTIVE', loc: 'Dunedin', views: 112, favs: 19, age: 5,
    imgs: ['mustard-wingback-armchair-1.jpg', 'mustard-wingback-armchair-2.jpg', 'mustard-wingback-armchair-3.jpg', 'mustard-wingback-armchair-4.jpg'],
    desc: 'Mid-century wingback armchair in a warm mustard fabric with button tufting on the back and tapered wooden legs. A real statement piece for a reading corner.\n\n• Measurements: W 82 x D 80 x H 101 cm, seat height 43 cm\n• Material: Polyester fabric, solid wood legs\n• Condition: Smoke-free, pet-free home; no stains; seat still firm\n• Included: Leaf-print cushion\n• Pickup / shipping: Pickup from Dunedin only (help loading provided)' },
  { id: 31, sellerId: 5, categoryId: 5, title: 'Bouclé Armchair & Ottoman', price: 220, originalPrice: 699, condition: 'LIKE_NEW', status: 'ACTIVE', loc: 'Dunedin', views: 104, favs: 22, age: 1,
    imgs: ['boucle-armchair-ottoman-1.jpg', 'boucle-armchair-ottoman-2.jpg', 'boucle-armchair-ottoman-3.jpg', 'boucle-armchair-ottoman-4.jpg'],
    desc: 'Cream bouclé lounge chair with a curved back, splayed oak legs and the matching round ottoman. Soft, cosy and very on-trend.\n\n• Measurements: Chair W 70 x D 75 x H 85 cm; ottoman 45 cm diameter\n• Material: Bouclé fabric, oak legs\n• Condition: Less than a year old; no pilling or marks\n• Included: Chair and ottoman\n• Pickup / shipping: Pickup from Dunedin, or freight at cost' },
  { id: 32, sellerId: 5, categoryId: 5, title: 'Handmade Clay Vases (set of 3)', price: 45, originalPrice: 120, condition: 'NEW', status: 'ACTIVE', loc: 'Dunedin', views: 48, favs: 10, age: 15,
    imgs: ['clay-vases-set-1.jpg', 'clay-vases-set-2.jpg', 'clay-vases-set-3.jpg', 'clay-vases-set-4.jpg'],
    desc: 'Three hand-built vases in a raw, sandy clay finish - one round, one tall, one bottle-shaped. Beautiful with dried stems.\n\n• Measurements: Heights 14, 17 and 20 cm\n• Material: Unglazed clay (decorative, not watertight)\n• Condition: New, never used\n• Included: All 3 vases\n• Pickup / shipping: Bubble-wrapped and double-boxed, tracked' },
  { id: 33, sellerId: 2, categoryId: 5, title: 'Non-stick Frying Pan & Saucepan Set', price: 45, originalPrice: 129, condition: 'LIKE_NEW', status: 'ACTIVE', loc: 'Wellington', views: 45, favs: 6, age: 11,
    imgs: ['frying-pan-saucepan-set-1.jpg', 'frying-pan-saucepan-set-2.jpg', 'frying-pan-saucepan-set-3.jpg', 'frying-pan-saucepan-set-4.jpg'],
    desc: 'Grey non-stick frying pan and matching saucepan with orange soft-touch handles and glass lids with wooden knobs. Induction compatible.\n\n• Included: 28 cm frying pan, 18 cm saucepan, 2 glass lids\n• Material: Aluminium with non-stick coating, induction base\n• Condition: Used a handful of times; coating perfect, no scratches\n• Pickup / shipping: Tracked courier or pickup in Wellington' },
  { id: 34, sellerId: 3, categoryId: 7, title: 'Olympia Manual Typewriter', price: 160, originalPrice: null, condition: 'GOOD', status: 'ACTIVE', loc: 'Christchurch', views: 121, favs: 23, age: 9,
    imgs: ['olympia-typewriter-1.jpg', 'olympia-typewriter-2.jpg', 'olympia-typewriter-3.jpg', 'olympia-typewriter-4.jpg'],
    desc: 'Vintage Olympia portable typewriter in dark green. Types cleanly - the photos show real test sheets. A fun piece for writers or as a display item.\n\n• Included: New black ribbon fitted, carry case\n• Material: Steel body, original keys\n• Condition: All keys, carriage return and bell work; some paint wear on the edges\n• Pickup / shipping: Pickup in Christchurch or insured courier' },
  { id: 35, sellerId: 3, categoryId: 7, title: 'Praktica MTL 3 Film Camera + 50 mm Lens', price: 140, originalPrice: null, condition: 'GOOD', status: 'ACTIVE', loc: 'Christchurch', views: 97, favs: 17, age: 6,
    imgs: ['praktica-mtl3-camera-1.jpg', 'praktica-mtl3-camera-2.jpg', 'praktica-mtl3-camera-3.jpg', 'praktica-mtl3-camera-4.jpg', 'praktica-mtl3-camera-5.jpg'],
    desc: 'Praktica MTL 3 35 mm SLR with a 50 mm f/1.8 lens. Fully mechanical, so it shoots at all speeds even without a battery; the light meter works with a fresh cell.\n\n• Included: Body, 50 mm lens, strap and lens cap\n• Material: Metal body, M42 screw mount\n• Condition: Shutter tested at all speeds; clear viewfinder; light brassing on the corners\n• Pickup / shipping: Insured tracked courier' },
  { id: 36, sellerId: 2, categoryId: 7, title: 'Sunburst Semi-Hollow Electric Guitar', price: 420, originalPrice: 900, condition: 'GOOD', status: 'ACTIVE', loc: 'Wellington', views: 134, favs: 25, age: 8,
    imgs: ['sunburst-semi-hollow-guitar-1.jpg', 'sunburst-semi-hollow-guitar-2.jpg', 'sunburst-semi-hollow-guitar-3.jpg', 'sunburst-semi-hollow-guitar-4.jpg'],
    desc: 'Semi-hollow electric guitar in a vintage sunburst with f-holes, twin humbuckers and a tune-o-matic bridge. Warm jazz and blues tones, and it can still bite with overdrive.\n\n• Included: Guitar and gig bag\n• Material: Laminated maple body, mahogany neck, rosewood fingerboard\n• Condition: Freshly set up with new strings; a couple of small dings on the lower bout\n• Pickup / shipping: Pickup in Wellington or insured courier in a guitar box' },
  { id: 37, sellerId: 4, categoryId: 6, title: 'Adidas Football Boots + Match Ball', price: 70, originalPrice: 260, condition: 'GOOD', status: 'ACTIVE', loc: 'Hamilton', views: 62, favs: 8, age: 12,
    imgs: ['adidas-football-boots-ball-1.jpg', 'adidas-football-boots-ball-2.jpg', 'adidas-football-boots-ball-3.jpg'],
    desc: 'Black Adidas Predator firm-ground football boots with the red collar, plus the Telstar match ball shown in the photos. One season of Saturday league.\n\n• Size: Men\'s US 9\n• Included: Boots and size 5 ball\n• Material: Synthetic upper, FG studs\n• Condition: Studs have plenty of life; ball holds air well\n• Pickup / shipping: Tracked courier from Hamilton' },
  { id: 38, sellerId: 2, categoryId: 6, title: 'Adidas Star Match Ball (size 5)', price: 35, originalPrice: 90, condition: 'LIKE_NEW', status: 'ACTIVE', loc: 'Wellington', views: 33, favs: 4, age: 16,
    imgs: ['adidas-star-match-ball-1.jpg', 'adidas-star-match-ball-2.jpg', 'adidas-star-match-ball-3.jpg'],
    desc: 'Adidas star-panel match ball in white and blue, thermally bonded for a true flight. Used for a few training sessions only.\n\n• Size: Size 5\n• Material: Thermally bonded PU panels\n• Condition: Holds air perfectly; no scuffs on the panels\n• Pickup / shipping: Tracked courier (deflated for posting)' },
  { id: 39, sellerId: 3, categoryId: 6, title: 'Scarpa Climbing Shoes', price: 65, originalPrice: 229, condition: 'GOOD', status: 'ACTIVE', loc: 'Christchurch', views: 47, favs: 7, age: 20,
    imgs: ['scarpa-climbing-shoes-1.jpg', 'scarpa-climbing-shoes-2.jpg', 'scarpa-climbing-shoes-3.jpg'],
    desc: 'Scarpa lace-up climbing shoes in tan suede with a flat, comfortable last - great for long gym sessions and all-day crag trips.\n\n• Size: EU 41\n• Material: Suede upper, Vibram rubber\n• Condition: Rubber has plenty left and no holes at the toe; washed and dried\n• Pickup / shipping: Tracked courier or pickup in Christchurch' },
  { id: 40, sellerId: 2, categoryId: 6, title: 'Black Single-Speed City Bike + Helmet', price: 320, originalPrice: 780, condition: 'GOOD', status: 'ACTIVE', loc: 'Wellington', views: 141, favs: 24, age: 3,
    imgs: ['single-speed-city-bike-1.jpg', 'single-speed-city-bike-2.jpg', 'single-speed-city-bike-3.jpg', 'single-speed-city-bike-4.jpg'],
    desc: 'Matte black single-speed city bike with a flip-flop hub (fixed or freewheel), riser bars and a sleek steel frame. Comes with the matching black road helmet.\n\n• Size: 54 cm frame (suits 170-182 cm riders)\n• Material: Hi-ten steel frame, 700c wheels\n• Condition: Serviced last month: new chain and brake pads; a few small scratches on the top tube\n• Included: Bike and helmet (M)\n• Pickup / shipping: Pickup in Wellington only' },
  { id: 41, sellerId: 2, categoryId: 3, title: 'Nike Downshifter Running Shoes', price: 50, originalPrice: 130, condition: 'GOOD', status: 'ACTIVE', loc: 'Wellington', views: 74, favs: 9, age: 7,
    imgs: ['nike-running-shoes-1.jpg', 'nike-running-shoes-2.jpg', 'nike-running-shoes-3.jpg', 'nike-running-shoes-4.jpg', 'nike-running-shoes-5.jpg'],
    desc: 'Nike running shoes in black mesh with the orange swoosh and "Running" heel print. Light, breathable and cushioned for everyday runs.\n\n• Size: Men\'s US 10\n• Material: Mesh upper, foam midsole, rubber outsole\n• Condition: Around 120 km on them; plenty of tread; washed and deodorised\n• Pickup / shipping: Tracked courier from Wellington' },
  { id: 42, sellerId: 5, categoryId: 6, title: 'Badminton Racket + Shuttlecocks', price: 30, originalPrice: 89, condition: 'LIKE_NEW', status: 'ACTIVE', loc: 'Dunedin', views: 28, favs: 3, age: 23,
    imgs: ['badminton-racket-shuttles-1.jpg', 'badminton-racket-shuttles-2.jpg', 'badminton-racket-shuttles-3.jpg', 'badminton-racket-shuttles-4.jpg'],
    desc: 'Lightweight red-framed badminton racket with a tight white string bed, plus a tube of feather shuttlecocks.\n\n• Included: Racket, cover and 6 feather shuttles\n• Material: Carbon-composite frame\n• Condition: Strings intact and tensioned; frame unmarked\n• Pickup / shipping: Tracked courier from Dunedin' },
  { id: 43, sellerId: 3, categoryId: 6, title: 'Green Roll-Top Hiking Pack', price: 95, originalPrice: 260, condition: 'LIKE_NEW', status: 'ACTIVE', loc: 'Christchurch', views: 69, favs: 12, age: 9,
    imgs: ['green-roll-top-hiking-pack-1.jpg', 'green-roll-top-hiking-pack-2.jpg', 'green-roll-top-hiking-pack-3.jpg', 'green-roll-top-hiking-pack-4.jpg'],
    desc: 'Forest-green roll-top hiking pack with mustard compression straps, padded back panel and hip belt. Perfect for overnight tramps.\n\n• Measurements: About 40 litres\n• Material: Water-resistant ripstop nylon\n• Condition: Used on two trips; no tears, buckles all intact\n• Pickup / shipping: Tracked courier from Christchurch' },
  { id: 44, sellerId: 4, categoryId: 6, title: 'Matte Black Road Bike', price: 650, originalPrice: 1600, condition: 'GOOD', status: 'ACTIVE', loc: 'Hamilton', views: 152, favs: 27, age: 2,
    imgs: ['matte-black-road-bike-1.jpg', 'matte-black-road-bike-2.jpg', 'matte-black-road-bike-3.jpg', 'matte-black-road-bike-4.jpg'],
    desc: 'Stealthy matte black road bike with drop bars and black bar tape, a slim saddle and deep-section wheels. Quick, light and great for weekend loops.\n\n• Size: 56 cm frame (suits 175-185 cm riders)\n• Material: Alloy frame, carbon fork\n• Condition: Serviced this month; new tyres; minor scuffs on the chainstay\n• Pickup / shipping: Pickup in Hamilton, or bike-box courier at cost' }
]

const favorites = new Set([2, 5])

// A listing has either a single `img` or an ordered `imgs` gallery; the first is the cover.
const listingImages = (l) => l.imgs || [l.img]
const coverUrl = (l) => asset(listingImages(l)[0])

// Days since the listing was posted; drives createdAt and the default "newest" sort.
const listingAge = (l) => l.age ?? l.id * 3

const toSummary = (l) => ({
  id: l.id,
  title: l.title,
  price: l.price,
  originalPrice: l.originalPrice,
  condition: l.condition,
  location: l.loc,
  negotiable: true,
  coverImageUrl: coverUrl(l),
  status: l.status,
  viewCount: l.views,
  favoriteCount: l.favs,
  createdAt: daysAgo(listingAge(l)),
  category: categoryById(l.categoryId),
  seller: publicUser(users[l.sellerId])
})
const toDetail = (l) => ({
  ...toSummary(l),
  description: l.desc,
  shippingFee: l.price > 60 ? 0 : 6.5,
  imageUrls: listingImages(l).map(asset),
  updatedAt: daysAgo(l.id),
  favorited: favorites.has(l.id)
})

const listingById = (id) => rawListings.find((l) => l.id === Number(id))

// ---------------------------------------------------------------------------
// Transaction graph for the static demo (no backend). Built so a visitor who
// "logs in" as ava@renova.local sees populated messages, offers, orders, and
// reviews — mirroring the backend seed. Records are stored raw and projected
// relative to the current user, so logging in as anyone else still works.
// ---------------------------------------------------------------------------

// Conversations: each has a buyer, a seller, a listing, and a message thread.
const rawConversations = [
  {
    id: 1, listingId: 6, buyerId: 1, sellerId: 2,
    messages: [
      { senderId: 1, body: 'Hi! Are the Run Star Hikes still available?', days: 2, mins: 0 },
      { senderId: 2, body: 'Yep, only worn a handful of times. Cleaned and ready to post.', days: 2, mins: 35 },
      { senderId: 1, body: 'Great — I just sent an offer at $65.', days: 1, mins: 0 },
      { senderId: 2, body: 'Accepted! I\'ll ship tomorrow and send tracking.', days: 1, mins: 20, unreadFor: 'buyer' }
    ]
  },
  {
    id: 2, listingId: 1, buyerId: 3, sellerId: 1,
    messages: [
      { senderId: 3, body: 'Is the satin wrap dress true to a size 10?', days: 1, mins: 0 },
      { senderId: 1, body: 'Runs slightly small — a comfy 8 to 10.', days: 1, mins: 25 },
      { senderId: 3, body: 'Perfect, putting an offer in now!', days: 0, mins: 90, unreadFor: 'seller' }
    ]
  },
  {
    id: 3, listingId: 2, buyerId: 4, sellerId: 1,
    messages: [
      { senderId: 4, body: 'Would you ship the leather tote to Hamilton?', days: 3, mins: 0 },
      { senderId: 1, body: 'Absolutely — free shipping on this one.', days: 3, mins: 40 }
    ]
  }
]

const convLastMsg = (c) => c.messages[c.messages.length - 1]
const convUnreadFor = (c, uid) => {
  const side = uid === c.buyerId ? 'buyer' : uid === c.sellerId ? 'seller' : null
  if (!side) return 0
  return c.messages.filter((m) => m.unreadFor === side).length
}
const msgTime = (m) => new Date(now - m.days * 86400000 + (m.mins || 0) * 60000).toISOString()

function conversationSummary(c, uid) {
  const l = listingById(c.listingId)
  const counterpartyId = uid === c.buyerId ? c.sellerId : c.buyerId
  const last = convLastMsg(c)
  return {
    id: c.id,
    listingId: c.listingId,
    listingTitle: l ? l.title : 'Listing',
    listingCoverImageUrl: l ? coverUrl(l) : null,
    listingStatus: l ? l.status : 'ACTIVE',
    counterparty: publicUser(users[counterpartyId]),
    role: uid === c.buyerId ? 'BUYER' : 'SELLER',
    lastMessagePreview: last ? last.body : '',
    lastMessageAt: last ? msgTime(last) : daysAgo(1),
    unreadCount: convUnreadFor(c, uid)
  }
}
function conversationDetail(c, uid) {
  return {
    conversation: conversationSummary(c, uid),
    messages: c.messages.map((m, i) => ({
      id: c.id * 100 + i,
      conversationId: c.id,
      senderId: m.senderId,
      senderName: users[m.senderId] ? users[m.senderId].displayName : 'User',
      body: m.body,
      createdAt: msgTime(m),
      readAt: m.unreadFor ? null : msgTime(m)
    }))
  }
}

// Offers: buyer makes an offer on a listing (the seller is the listing owner).
const rawOffers = [
  { id: 1, listingId: 1, buyerId: 3, amount: 55, message: 'Love this dress! Would $55 work?', status: 'PENDING', fromSeller: false, days: 1 },
  { id: 2, listingId: 2, buyerId: 4, amount: 80, message: '$80 shipped to Hamilton?', status: 'PENDING', fromSeller: false, days: 2 },
  { id: 3, listingId: 6, buyerId: 1, amount: 65, message: 'Would you take $65?', status: 'ACCEPTED', fromSeller: false, days: 1, respondedDays: 1 },
  { id: 4, listingId: 7, buyerId: 1, amount: 14, message: '$14 for the yoga mat?', status: 'PENDING', fromSeller: false, days: 0 }
]
function offerResponse(o) {
  const l = listingById(o.listingId)
  return {
    id: o.id,
    listingId: o.listingId,
    listingTitle: l ? l.title : 'Listing',
    listingCoverImageUrl: l ? coverUrl(l) : null,
    buyer: publicUser(users[o.buyerId]),
    seller: publicUser(users[l ? l.sellerId : 1]),
    amount: o.amount,
    message: o.message,
    status: o.status,
    fromSeller: o.fromSeller,
    parentOfferId: null,
    createdAt: daysAgo(o.days),
    respondedAt: o.respondedDays != null ? daysAgo(o.respondedDays) : null
  }
}

// Orders: a buyer's purchase from a seller, in various lifecycle states.
const rawOrders = [
  { id: 1, num: 'RN10231007', listingId: 6, buyerId: 1, sellerId: 2, agreed: 65, ship: 0, status: 'SHIPPED', days: 2, carrier: 'NZ Post', tracking: 'NZ1234567', name: 'Ava Thompson', phone: '+64 21 555 0142', addr: '14 Ponsonby Rd, Auckland 1011' },
  { id: 2, num: 'RN10240412', listingId: 9, buyerId: 1, sellerId: 2, agreed: 24, ship: 0, status: 'COMPLETED', days: 14, carrier: 'CourierPost', tracking: 'NZ9988776', name: 'Ava Thompson', phone: '+64 21 555 0142', addr: '14 Ponsonby Rd, Auckland 1011' },
  { id: 3, num: 'RN10255533', listingId: 3, buyerId: 3, sellerId: 1, agreed: 54, ship: 0, status: 'PAID', days: 2, name: 'Maya Flores', phone: '+64 21 555 0188', addr: '5 Colombo St, Christchurch 8011' },
  { id: 4, num: 'RN10260088', listingId: 4, buyerId: 5, sellerId: 1, agreed: 28, ship: 0, status: 'COMPLETED', days: 10, carrier: 'NZ Post', tracking: 'NZ5544332', name: 'Ivy Bennett', phone: '+64 21 555 0170', addr: '88 George St, Dunedin 9016' }
]
function orderResponse(o) {
  const l = listingById(o.listingId)
  const paid = ['PAID', 'SHIPPED', 'DELIVERED', 'COMPLETED'].includes(o.status)
  const shipped = ['SHIPPED', 'DELIVERED', 'COMPLETED'].includes(o.status)
  const done = o.status === 'COMPLETED'
  return {
    id: o.id,
    orderNumber: o.num,
    listingId: o.listingId,
    listingTitle: l ? l.title : 'Listing',
    listingCoverImageUrl: l ? coverUrl(l) : null,
    buyer: publicUser(users[o.buyerId]),
    seller: publicUser(users[o.sellerId]),
    agreedPrice: o.agreed,
    shippingFee: o.ship,
    totalAmount: o.agreed + o.ship,
    shippingName: o.name,
    shippingPhone: o.phone,
    shippingAddress: o.addr,
    buyerNote: null,
    trackingNumber: shipped ? o.tracking || null : null,
    carrier: shipped ? o.carrier || null : null,
    status: o.status,
    createdAt: daysAgo(o.days),
    paidAt: paid ? daysAgo(o.days) : null,
    shippedAt: shipped ? daysAgo(Math.max(0, o.days - 1)) : null,
    deliveredAt: done ? daysAgo(Math.max(0, o.days - 3)) : null,
    completedAt: done ? daysAgo(Math.max(0, o.days - 3)) : null,
    cancelledAt: null,
    cancelReason: null
  }
}

// Reviews left on completed orders (reviewee gets the rating).
const rawReviews = [
  { id: 1, orderId: 4, listingId: 4, reviewerId: 5, revieweeId: 1, rating: 5, comment: 'Beautiful clutch, even nicer in person. Fast shipping!', role: 'BUYER_REVIEWS_SELLER', days: 9 },
  { id: 2, orderId: 2, listingId: 9, reviewerId: 1, revieweeId: 2, rating: 5, comment: 'Mugs arrived perfectly packed. Thanks Liam!', role: 'BUYER_REVIEWS_SELLER', days: 12 },
  { id: 3, orderId: 4, listingId: 4, reviewerId: 1, revieweeId: 5, rating: 5, comment: 'Smooth, friendly buyer. Recommended.', role: 'SELLER_REVIEWS_BUYER', days: 9 }
]
function reviewResponse(r) {
  const l = listingById(r.listingId)
  return {
    id: r.id,
    orderId: r.orderId,
    listingTitle: l ? l.title : 'Listing',
    reviewer: publicUser(users[r.reviewerId]),
    reviewee: publicUser(users[r.revieweeId]),
    rating: r.rating,
    comment: r.comment,
    role: r.role,
    createdAt: daysAgo(r.days)
  }
}

function paginate(items, params = {}) {
  const page = Number(params.page ?? 0)
  const size = Number(params.size ?? 12)
  const start = page * size
  const slice = items.slice(start, start + size)
  return {
    content: slice,
    page,
    size,
    totalElements: items.length,
    totalPages: Math.max(1, Math.ceil(items.length / size)),
    first: page === 0,
    last: start + size >= items.length,
    empty: slice.length === 0
  }
}

function searchListings(params = {}) {
  let items = rawListings.filter((l) => l.status !== 'REMOVED')
  if (params.categoryId) items = items.filter((l) => l.categoryId === Number(params.categoryId))
  // The real API uses `keyword`; match title + description.
  const kw = params.keyword ?? params.q
  if (kw) {
    const q = String(kw).toLowerCase()
    items = items.filter((l) => l.title.toLowerCase().includes(q) || l.desc.toLowerCase().includes(q))
  }
  if (params.condition) items = items.filter((l) => l.condition === params.condition)
  if (params.minPrice != null && params.minPrice !== '') items = items.filter((l) => l.price >= Number(params.minPrice))
  if (params.maxPrice != null && params.maxPrice !== '') items = items.filter((l) => l.price <= Number(params.maxPrice))
  if (params.location) {
    const loc = String(params.location).toLowerCase()
    items = items.filter((l) => (l.loc || '').toLowerCase().includes(loc))
  }
  if (params.sort === 'price_asc') items = [...items].sort((a, b) => a.price - b.price)
  else if (params.sort === 'price_desc') items = [...items].sort((a, b) => b.price - a.price)
  else if (params.sort === 'popular') items = [...items].sort((a, b) => b.favs - a.favs)
  else items = [...items].sort((a, b) => listingAge(a) - listingAge(b) || b.id - a.id)
  return paginate(items.map(toSummary), params)
}

function currentUserId(config) {
  const auth = config.headers?.Authorization || config.headers?.authorization || ''
  const m = /demo\.(\d+)/.exec(auth)
  return m ? Number(m[1]) : 1
}

// Returns the unwrapped payload for a request, or throws { status } for 404.
function route(config) {
  const method = (config.method || 'get').toUpperCase()
  const path = (config.url || '').split('?')[0].replace(/\/$/, '')
  const params = config.params || {}
  const body = parseBody(config.data)
  const id = (re) => {
    const m = re.exec(path)
    return m ? Number(m[1]) : null
  }

  // ---- auth ----
  if (path === '/auth/login' && method === 'POST') {
    const u = Object.values(users).find((x) => x.email === (body.email || '').toLowerCase()) || users[1]
    return { token: `demo.${u.id}`, expiresInMinutes: 120, user: summaryUser(u) }
  }
  if (path === '/auth/signup' && method === 'POST') {
    const u = { ...users[1], email: body.email || 'guest@renova.local', displayName: body.displayName || 'Demo Guest' }
    return { token: 'demo.1', expiresInMinutes: 120, user: summaryUser(u) }
  }
  if (path === '/auth/me' || path === '/users/me') {
    return method === 'PUT' ? { ...summaryUser(users[currentUserId(config)]), ...body } : summaryUser(users[currentUserId(config)])
  }

  // ---- public catalogue ----
  if (path === '/public/categories') return categories
  if (path === '/public/listings') return searchListings(params)
  let m
  if ((m = /^\/public\/listings\/(\d+)$/.exec(path))) {
    const l = rawListings.find((x) => x.id === Number(m[1]))
    if (!l) throw { status: 404, message: 'Listing not found' }
    return toDetail(l)
  }
  if ((m = /^\/public\/users\/(\d+)\/listings$/.exec(path))) {
    return paginate(rawListings.filter((l) => l.sellerId === Number(m[1])).map(toSummary), params)
  }
  if ((m = /^\/public\/users\/(\d+)\/reviews$/.exec(path))) {
    return rawReviews.filter((r) => r.revieweeId === Number(m[1])).map(reviewResponse)
  }
  if ((m = /^\/public\/users\/(\d+)$/.exec(path))) {
    const u = users[Number(m[1])]
    if (!u) throw { status: 404, message: 'User not found' }
    return publicUser(u)
  }

  // ---- listings (auth) ----
  if (path === '/listings/mine') return paginate(rawListings.filter((l) => l.sellerId === currentUserId(config)).map(toSummary), params)
  if (path === '/listings/favorites') return paginate(rawListings.filter((l) => favorites.has(l.id)).map(toSummary), params)
  if ((m = /^\/listings\/(\d+)\/favorite$/.exec(path)) && method === 'POST') {
    const lid = Number(m[1])
    if (favorites.has(lid)) favorites.delete(lid)
    else favorites.add(lid)
    return { favorited: favorites.has(lid) }
  }
  if (path === '/listings' && method === 'POST') return { ...toDetail(rawListings[0]), id: 999, title: body.title || 'New listing' }
  if ((m = /^\/listings\/(\d+)$/.exec(path))) return { id: Number(m[1]) }

  // ---- offers (relative to the logged-in user) ----
  const uid = currentUserId(config)
  if (path === '/offers/received') {
    // offers on listings the current user is selling
    const mine = rawOffers.filter((o) => { const l = listingById(o.listingId); return l && l.sellerId === uid })
    return paginate(mine.map(offerResponse), params)
  }
  if (path === '/offers/sent') {
    const mine = rawOffers.filter((o) => o.buyerId === uid)
    return paginate(mine.map(offerResponse), params)
  }
  if ((m = /^\/offers\/(\d+)\/(accept|reject|counter|withdraw|accept-counter)$/.exec(path)) && method === 'POST') {
    const o = rawOffers.find((x) => x.id === Number(m[1]))
    if (!o) throw { status: 404, message: 'Offer not found' }
    const next = {
      accept: 'ACCEPTED', reject: 'REJECTED', counter: 'COUNTERED',
      withdraw: 'WITHDRAWN', 'accept-counter': 'ACCEPTED'
    }[m[2]]
    o.status = next
    o.respondedDays = 0
    // Accepting an offer reserves its listing and rejects the siblings,
    // mirroring the backend so the UI reflects a real state change.
    if (m[2] === 'accept' || m[2] === 'accept-counter') {
      const listing = listingById(o.listingId)
      if (listing) listing.status = 'RESERVED'
      rawOffers.forEach((x) => {
        if (x.id !== o.id && x.listingId === o.listingId && x.status === 'PENDING') x.status = 'REJECTED'
      })
    }
    return offerResponse(o)
  }

  // ---- orders ----
  if (path === '/orders/buying') {
    return paginate(rawOrders.filter((o) => o.buyerId === uid).map(orderResponse), params)
  }
  if (path === '/orders/selling') {
    return paginate(rawOrders.filter((o) => o.sellerId === uid).map(orderResponse), params)
  }
  if ((m = /^\/orders\/(\d+)\/reviews$/.exec(path))) {
    return rawReviews.filter((r) => r.orderId === Number(m[1])).map(reviewResponse)
  }
  if ((m = /^\/orders\/(\d+)\/(pay|ship|confirm-receipt|cancel)$/.exec(path)) && method === 'POST') {
    const o = rawOrders.find((x) => x.id === Number(m[1]))
    if (!o) throw { status: 404, message: 'Order not found' }
    // Advance the in-memory order so the detail page re-renders the new state
    // (and, critically, returns a full order object — not { ok: true } — which
    // the page assigns to `order.value`).
    if (m[2] === 'pay') o.status = 'PAID'
    else if (m[2] === 'ship') { o.status = 'SHIPPED'; o.carrier = body.carrier || o.carrier; o.tracking = body.trackingNumber || o.tracking }
    else if (m[2] === 'confirm-receipt') { o.status = 'COMPLETED'; const l = listingById(o.listingId); if (l) l.status = 'SOLD' }
    else if (m[2] === 'cancel') o.status = 'CANCELLED'
    return orderResponse(o)
  }
  if ((m = /^\/orders\/(\d+)$/.exec(path))) {
    const o = rawOrders.find((x) => x.id === Number(m[1]))
    if (!o) throw { status: 404, message: 'Order not found' }
    return orderResponse(o)
  }
  if (path === '/orders' && method === 'POST') {
    return orderResponse(rawOrders[0]) // demo checkout returns a sample order
  }

  // ---- conversations ----
  if (path === '/conversations') {
    if (method === 'POST') {
      // starting/continuing a conversation: return the matching thread or the first one
      const lid = Number(body.listingId)
      const c = rawConversations.find((x) => x.listingId === lid) || rawConversations[0]
      return conversationDetail(c, uid)
    }
    const mine = rawConversations
      .filter((c) => c.buyerId === uid || c.sellerId === uid)
      .sort((a, b) => new Date(conversationSummary(b, uid).lastMessageAt) - new Date(conversationSummary(a, uid).lastMessageAt))
    return mine.map((c) => conversationSummary(c, uid))
  }
  if (path === '/conversations/unread-count') {
    const total = rawConversations
      .filter((c) => c.buyerId === uid || c.sellerId === uid)
      .reduce((sum, c) => sum + convUnreadFor(c, uid), 0)
    return { count: total }
  }
  if ((m = /^\/conversations\/(\d+)\/messages$/.exec(path)) && method === 'POST') {
    return {
      id: Date.now(), conversationId: Number(m[1]), senderId: uid,
      senderName: users[uid] ? users[uid].displayName : 'You',
      body: body.body || '', createdAt: new Date().toISOString(), readAt: null
    }
  }
  if ((m = /^\/conversations\/(\d+)$/.exec(path))) {
    const c = rawConversations.find((x) => x.id === Number(m[1]))
    if (!c) throw { status: 404, message: 'Conversation not found' }
    return conversationDetail(c, uid)
  }

  // ---- reviews ----
  if (path === '/reviews' && method === 'POST') {
    const order = rawOrders.find((x) => x.id === Number(body.orderId))
    if (!order) throw { status: 404, message: 'Order not found' }
    const buyerReviews = order.buyerId === uid
    const revieweeId = buyerReviews ? order.sellerId : order.buyerId
    const review = {
      id: Date.now(),
      orderId: order.id,
      listingId: order.listingId,
      reviewerId: uid,
      revieweeId,
      rating: Number(body.rating) || 5,
      comment: body.comment || '',
      role: buyerReviews ? 'BUYER_REVIEWS_SELLER' : 'SELLER_REVIEWS_BUYER',
      days: 0
    }
    rawReviews.push(review)
    return reviewResponse(review)
  }

  // ---- image upload ----
  if (path === '/uploads/images' && method === 'POST') {
    // config.data is a FormData; turn each real File into an object URL so the
    // uploaded photo shows in the preview even though there's no backend.
    const files = (config.data && typeof config.data.getAll === 'function')
      ? config.data.getAll('files') : []
    const images = files.map((f, i) => ({
      url: (typeof URL !== 'undefined' && typeof URL.createObjectURL === 'function' && f && f.size != null)
        ? URL.createObjectURL(f)
        : asset('fashion-dress.jpg'),
      contentType: (f && f.type) || 'image/jpeg',
      size: (f && f.size) || 0,
      originalName: (f && f.name) || `image-${i + 1}.jpg`
    }))
    return { images }
  }

  // default: succeed quietly so the demo never shows a hard error
  if (method === 'GET') return null
  return { ok: true }
}

function parseBody(data) {
  if (!data) return {}
  if (typeof data === 'object') return data
  try {
    return JSON.parse(data)
  } catch {
    return {}
  }
}

// Axios adapter: resolve with an ApiResponse-shaped envelope, or reject like the real API.
export function demoAdapter(config) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      try {
        const data = route(config)
        resolve({
          data: { success: true, message: '', data, timestamp: new Date().toISOString() },
          status: 200,
          statusText: 'OK',
          headers: { 'content-type': 'application/json' },
          config
        })
      } catch (err) {
        const status = err?.status || 500
        reject({
          response: {
            status,
            data: { success: false, message: err?.message || 'Demo request failed', data: null }
          },
          config,
          isAxiosError: true
        })
      }
    }, 180) // small latency so loading states are visible
  })
}

export const DEMO_MODE = import.meta.env.VITE_RENOVA_DEMO === 'true'
