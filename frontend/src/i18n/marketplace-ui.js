export const marketplaceUi = {
  en: {
    eyebrow: 'A second-hand marketplace',
    heroTitle: 'Good finds. Fresh beginnings.',
    heroBody: 'Give something you love a new home. Find something that feels like you.',
    browse: 'Explore the finds', sell: 'Sell an item',
    photoAlt: 'A matte white ceramic vase on a light grey background',
    photoCaption: 'Less new. More character.',
    categories: 'Find your kind of thing', allCategories: 'All categories',
    latest: 'Fresh on the shelf', latestBody: 'Pre-loved pieces, ready for their next chapter.',
    emptyTitle: 'The next good find starts with you.',
    emptyBody: 'No items have been listed yet. Make a little room at home and give your first item a second life.',
    loadError: 'We couldn’t load the latest finds.', loadErrorBody: 'Please try again in a moment.',
    categoryError: 'Categories couldn’t be loaded.',
    workflowLabel: 'A little space. A new story.', workflowTitle: 'Pass it on, in a few simple steps.',
    steps: {
      discover: { title: 'Find your next favorite', body: 'Browse the listings, check the details and save what catches your eye.' },
      connect: { title: 'Talk it through', body: 'Message the seller, ask a question or make an offer on a negotiable item.' },
      exchange: { title: 'Try the full journey', body: 'Explore checkout and order tracking. Payments and shipping are simulated in this portfolio demo.' }
    },
    footerNote: 'A portfolio project. Payments and shipping are simulated.',
    footerCopyright: 'Made for things worth keeping.',
    menu: 'Open navigation', navigation: 'Main navigation', skip: 'Skip to content',
    categoryNames: {
      fashion: 'Fashion', beauty: 'Beauty', collectibles: 'Collectibles', electronics: 'Electronics', clothing: 'Clothing', furniture: 'Furniture', books: 'Books',
      sports: 'Sports', toys: 'Toys & games', home: 'Home & living', vehicles: 'Vehicles', other: 'Other finds',
      women: 'Women’s fashion', men: 'Men’s fashion', shoes: 'Shoes', bags: 'Bags & accessories'
    }
  },
  zh: {
    eyebrow: '让闲置，遇见新的喜欢',
    heroTitle: '旧物有故事，好物有新家。',
    heroBody: '把喜欢过的好物传递下去，也在这里找到属于你的小惊喜。',
    browse: '发现好物', sell: '发布闲置',
    photoAlt: '浅灰背景上的白色哑光陶瓷花瓶', photoCaption: '不必全新，也能心动。',
    categories: '从喜欢的分类逛起', allCategories: '全部分类',
    latest: '刚刚上架', latestBody: '被珍惜过的好物，等你开启下一段故事。',
    emptyTitle: '第一件好物，可以从你开始。',
    emptyBody: '这里还没有上架的物品。给家里腾一点空间，让你的闲置遇见新的主人。',
    loadError: '暂时无法加载最新好物。', loadErrorBody: '请稍后重试。',
    categoryError: '分类暂时无法加载。',
    workflowLabel: '腾一点空间，续一段故事', workflowTitle: '让好物传递，就是这么简单。',
    steps: {
      discover: { title: '找到心动好物', body: '浏览物品、查看详情，把喜欢的先收藏起来。' },
      connect: { title: '和卖家聊一聊', body: '询问细节，或给接受议价的物品发出报价。' },
      exchange: { title: '体验完整交易', body: '探索下单与订单跟踪。本作品集项目中的支付和配送均为模拟。' }
    },
    footerNote: '作品集演示项目，支付和配送均为模拟。',
    footerCopyright: '让值得珍惜的好物继续陪伴。',
    menu: '打开导航', navigation: '主导航', skip: '跳至主要内容',
    categoryNames: {
      fashion: '服饰穿搭', beauty: '美妆护理', collectibles: '收藏', electronics: '数码电子', clothing: '服饰', furniture: '家具', books: '图书',
      sports: '运动户外', toys: '玩具游戏', home: '家居生活', vehicles: '交通工具', other: '其他好物',
      women: '女装', men: '男装', shoes: '鞋履', bags: '包袋配饰'
    }
  }
}

export function categoryLabel(category, t, te) {
  const key = `marketplaceUi.categoryNames.${category.slug}`
  return te(key) ? t(key) : category.name
}
