const POPULAR_CATEGORIES = [
  { id: 1, name: 'Home Cleaning', icon: '🧹', color: '#ff9500' },
  { id: 2, name: 'Lawn & Garden', icon: '🌱', color: '#34c759' },
  { id: 3, name: 'Handyman Work', icon: '🔧', color: '#5856d6' },
  { id: 4, name: 'Removals', icon: '📦', color: '#af52de' },
  { id: 5, name: 'House Painting', icon: '🎨', color: '#ff2d55' },
  { id: 6, name: 'Computers & IT', icon: '💻', color: '#0071e3' },
];

export const CategoryGrid = () => {
  const handleCategoryClick = (categoryName) => {
    console.log(`Filtering by category: ${categoryName}`);
    // Future integration: redirect to /listings?category=categoryName
  };

  return (
    <div style={{ margin: '2.5rem 0' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
        <h3 style={{ fontSize: '1.25rem', fontWeight: '700' }}>Browse Popular Services</h3>
        <a href="#" style={{ fontSize: '0.85rem', fontWeight: '600', color: 'var(--accent)' }}>View All</a>
      </div>

      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
        gap: '1rem'
      }}>
        {POPULAR_CATEGORIES.map((category) => (
          <button
            key={category.id}
            onClick={() => handleCategoryClick(category.name)}
            style={{
              backgroundColor: 'var(--bg-secondary)',
              border: '1px solid var(--border)',
              borderRadius: 'var(--radius-md)',
              padding: '1.5rem 1rem',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '0.5rem',
              transition: 'all 0.15s cubic-bezier(0.4, 0, 0.2, 1)',
              boxShadow: 'var(--shadow-sm)'
            }}
            onMouseOver={(e) => {
              e.currentTarget.style.borderColor = 'var(--accent)';
              e.currentTarget.style.transform = 'translateY(-2px)';
              e.currentTarget.style.boxShadow = 'var(--shadow-md)';
            }}
            onMouseOut={(e) => {
              e.currentTarget.style.borderColor = 'var(--border)';
              e.currentTarget.style.transform = 'none';
              e.currentTarget.style.boxShadow = 'var(--shadow-sm)';
            }}
          >
            <span style={{ fontSize: '2.2rem', marginBottom: '0.2rem' }}>{category.icon}</span>
            <span style={{ fontSize: '0.85rem', fontWeight: '700', color: 'var(--text-primary)' }}>
              {category.name}
            </span>
          </button>
        ))}
      </div>
    </div>
  );
};
