import React from 'react'

export function Badge({
  children,
  color = '#F5A623',
}: {
  children: React.ReactNode
  color?: string
}) {
  return (
    <span
      className="text-[10px] font-semibold px-2 py-0.5 rounded-full uppercase tracking-wider inline-flex items-center"
      style={{ backgroundColor: `${color}22`, color }}
    >
      {children}
    </span>
  )
}
